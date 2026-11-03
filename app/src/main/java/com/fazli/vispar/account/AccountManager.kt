package com.fazli.vispar.account

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.util.Log
import com.fazli.vispar.BuildConfig
import com.fazli.vispar.data.api.Api
import com.fazli.vispar.utils.DeviceUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlinx.serialization.json.put
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import java.io.IOException

@Serializable
data class Profile(
    val id: Long,
    val phone: String = "",
    @SerialName("display_name") val displayName: String = "",
    val language: String = "fa",
    /** Set by an admin; only shows a blue tick on the profile. */
    val verified: Boolean = false,
    @SerialName("created_at") val createdAt: String = "",
)

@Serializable
data class Device(
    val id: Long,
    val name: String = "",
    val kind: String = "phone",
    @SerialName("app_version") val appVersion: String = "",
    @SerialName("last_seen_at") val lastSeenAt: String = "",
    val current: Boolean = false,
)

@Serializable
private data class TokenResponse(
    @SerialName("access_token") val accessToken: String,
    @SerialName("refresh_token") val refreshToken: String,
    @SerialName("expires_in") val expiresIn: Long = 900,
    val user: Profile? = null,
)

@Serializable
private data class DevicesResponse(val devices: List<Device> = emptyList())

/** A failed call, with the server's error code ("wrong_code", "cooldown", …) or "network". */
class ApiException(val code: String, message: String, val status: Int = 0) : IOException(message)

sealed interface AccountState {
    data object SignedOut : AccountState
    data class SignedIn(val profile: Profile) : AccountState
}

/**
 * Phone sign-in and the signed-in session.
 *
 * Tokens live in private SharedPreferences that are excluded from backups. The access token is
 * short-lived; [authed] refreshes it transparently (the refresh token rotates on every use).
 */
object AccountManager {
    private const val TAG = "Account"
    private const val PREFS = "vispar_account"
    private val JSON_TYPE = "application/json; charset=utf-8".toMediaType()

    private lateinit var prefs: SharedPreferences
    private lateinit var appContext: Context
    private val refreshLock = Mutex()

    private val _state = MutableStateFlow<AccountState>(AccountState.SignedOut)
    val state: StateFlow<AccountState> = _state.asStateFlow()

    val isSignedIn: Boolean get() = _state.value is AccountState.SignedIn

    private val _needsName = MutableStateFlow(false)
    /** True right after a first sign-in, until the user has chosen a display name. */
    val needsName: StateFlow<Boolean> = _needsName.asStateFlow()

    fun init(context: Context) {
        if (::prefs.isInitialized) return
        appContext = context.applicationContext
        prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val profile = prefs.getString("profile", null)?.let {
            runCatching { Api.json.decodeFromString<Profile>(it) }.getOrNull()
        }
        if (profile != null && prefs.getString("refresh", null) != null) {
            _state.value = AccountState.SignedIn(profile)
            _needsName.value = prefs.getBoolean("needs_name", false)
        }
    }

    // ---- Sign in ------------------------------------------------------------------------------

    /** Sends an SMS code. Returns the seconds to wait before another code can be requested. */
    suspend fun requestCode(phone: String): Int {
        val body = buildJsonObject { put("phone", phone) }
        val res = call(post("/auth/otp/request", body))
        return res["resend_in"]?.jsonPrimitive?.content?.toIntOrNull() ?: 60
    }

    suspend fun verifyCode(phone: String, code: String): Profile {
        val body = buildJsonObject {
            put("phone", phone)
            put("code", code)
            put("device", buildJsonObject {
                put("name", deviceName())
                put("kind", deviceKind())
                put("app_version", BuildConfig.VERSION_NAME)
            })
        }
        val tokens = Api.json.decodeFromJsonElement(TokenResponse.serializer(), call(post("/auth/otp/verify", body)))
        val profile = tokens.user ?: throw ApiException("internal", "no profile in response")
        saveTokens(tokens)
        setNeedsName(profile.displayName.isBlank())
        saveProfile(profile)
        SyncManager.onSignedIn(appContext)
        return profile
    }

    // ---- Profile --------------------------------------------------------------------------------

    suspend fun reloadProfile(): Profile? = runCatching {
        val profile = Api.json.decodeFromJsonElement(Profile.serializer(), authedJson(Request.Builder().url(Api.BASE + "/me").get()))
        saveProfile(profile)
        profile
    }.onFailure { Log.w(TAG, "reloadProfile: ${it.message}") }.getOrNull()

    suspend fun updateName(name: String): Profile {
        val body = buildJsonObject { put("display_name", name.trim()) }
        val profile = Api.json.decodeFromJsonElement(
            Profile.serializer(),
            authedJson(Request.Builder().url(Api.BASE + "/me").patch(body.toBody()))
        )
        saveProfile(profile)
        if (profile.displayName.isNotBlank()) setNeedsName(false)
        return profile
    }

    /** Lets a new user continue without a name (it can be set later from the profile). */
    fun skipName() = setNeedsName(false)

    private fun setNeedsName(value: Boolean) {
        prefs.edit().putBoolean("needs_name", value).apply()
        _needsName.value = value
    }

    suspend fun devices(): List<Device> =
        Api.json.decodeFromJsonElement(DevicesResponse.serializer(), authedJson(Request.Builder().url(Api.BASE + "/me/devices").get())).devices

    suspend fun revokeDevice(id: Long) {
        authed(Request.Builder().url(Api.BASE + "/me/devices/$id").delete()).close()
    }

    /** Signs this device out on the server too (best effort), then clears the local session. */
    suspend fun signOut() {
        runCatching { authed(Request.Builder().url(Api.BASE + "/auth/logout").post(EMPTY)).close() }
        clearSession()
    }

    /** Permanently deletes the account and everything synced to it. */
    suspend fun deleteAccount() {
        authed(Request.Builder().url(Api.BASE + "/me").delete()).use { }
        clearSession()
        SyncManager.forget(appContext)
    }

    // ---- Authenticated calls ------------------------------------------------------------------

    suspend fun authedJson(builder: Request.Builder): JsonObject = authed(builder).use { parse(it) }

    /**
     * Runs a request with the access token, refreshing it once if it expired. A rejected refresh
     * (signed out elsewhere, blocked, deleted) ends the local session.
     */
    suspend fun authed(builder: Request.Builder): Response = withContext(Dispatchers.IO) {
        val token = validAccessToken() ?: throw ApiException("signed_out", "not signed in", 401)
        var res = Api.client.newCall(builder.header("Authorization", "Bearer $token").build()).execute()
        if (res.code == 401) {
            res.close()
            val fresh = refresh(force = true) ?: throw ApiException("signed_out", "session ended", 401)
            res = Api.client.newCall(builder.header("Authorization", "Bearer $fresh").build()).execute()
        }
        if (!res.isSuccessful) {
            res.use { throw errorOf(it) }
        }
        res
    }

    private suspend fun validAccessToken(): String? {
        val access = prefs.getString("access", null)
        val exp = prefs.getLong("access_exp", 0)
        if (access != null && System.currentTimeMillis() < exp - 30_000) return access
        return refresh(force = false)
    }

    private suspend fun refresh(force: Boolean): String? = refreshLock.withLock {
        // Another caller may have refreshed while we waited.
        val access = prefs.getString("access", null)
        if (!force && access != null && System.currentTimeMillis() < prefs.getLong("access_exp", 0) - 30_000) return access
        val refreshToken = prefs.getString("refresh", null) ?: return null
        val body = buildJsonObject { put("refresh_token", refreshToken) }
        try {
            val json = call(post("/auth/refresh", body))
            val tokens = Api.json.decodeFromJsonElement(TokenResponse.serializer(), json)
            saveTokens(tokens)
            tokens.accessToken
        } catch (e: ApiException) {
            if (e.status == 401 || e.status == 403) {
                Log.i(TAG, "refresh rejected (${e.code}), signing out")
                clearSession()
            }
            null
        }
    }

    // ---- Plumbing -----------------------------------------------------------------------------

    private suspend fun call(request: Request): JsonObject = withContext(Dispatchers.IO) {
        try {
            Api.client.newCall(request).execute().use { parse(it) }
        } catch (e: ApiException) {
            throw e
        } catch (e: IOException) {
            throw ApiException("network", e.message ?: "network error")
        }
    }

    private fun parse(res: Response): JsonObject {
        if (!res.isSuccessful) throw errorOf(res)
        val text = res.body?.string().orEmpty()
        if (text.isBlank()) return JsonObject(emptyMap())
        return Api.json.parseToJsonElement(text).jsonObject
    }

    private fun errorOf(res: Response): ApiException {
        val text = runCatching { res.body?.string() }.getOrNull().orEmpty()
        val err = runCatching { Api.json.parseToJsonElement(text).jsonObject["error"]?.jsonObject }.getOrNull()
        val code = err?.get("code")?.jsonPrimitive?.content ?: "http_${res.code}"
        val message = err?.get("message")?.jsonPrimitive?.content ?: "HTTP ${res.code}"
        return ApiException(code, message, res.code)
    }

    private fun post(path: String, body: JsonObject) = Request.Builder().url(Api.BASE + path).post(body.toBody()).build()

    private fun JsonObject.toBody(): RequestBody = toString().toRequestBody(JSON_TYPE)

    private val EMPTY = "{}".toRequestBody("application/json".toMediaType())

    private fun saveTokens(t: TokenResponse) {
        prefs.edit()
            .putString("access", t.accessToken)
            .putString("refresh", t.refreshToken)
            .putLong("access_exp", System.currentTimeMillis() + t.expiresIn * 1000)
            .apply()
    }

    private fun saveProfile(profile: Profile) {
        prefs.edit().putString("profile", Api.json.encodeToString(Profile.serializer(), profile)).apply()
        _state.value = AccountState.SignedIn(profile)
    }

    private fun clearSession() {
        prefs.edit().clear().apply()
        _needsName.value = false
        _state.value = AccountState.SignedOut
    }

    private fun deviceKind(): String = when {
        DeviceUtils.isTv(appContext) -> "tv"
        appContext.resources.configuration.smallestScreenWidthDp >= 600 -> "tablet"
        else -> "phone"
    }

    private fun deviceName(): String {
        val maker = Build.MANUFACTURER.orEmpty().replaceFirstChar { it.uppercase() }
        val model = Build.MODEL.orEmpty()
        return (if (model.startsWith(maker, ignoreCase = true)) model else "$maker $model").trim().take(60)
    }
}
