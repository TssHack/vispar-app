package com.fazli.vispar.screens

import android.icu.text.DateFormat
import android.icu.util.ULocale
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Logout
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.CloudSync
import androidx.compose.material.icons.rounded.DeleteForever
import androidx.compose.material.icons.rounded.Devices
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Favorite
import androidx.compose.material.icons.rounded.Lock
import androidx.compose.material.icons.rounded.Person
import androidx.compose.material.icons.rounded.PhoneAndroid
import androidx.compose.material.icons.rounded.Sms
import androidx.compose.material.icons.rounded.Sync
import androidx.compose.material.icons.rounded.Tablet
import androidx.compose.material.icons.rounded.Tv
import androidx.compose.material.icons.rounded.Verified
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.fazli.vispar.R
import com.fazli.vispar.account.AccountManager
import com.fazli.vispar.account.AccountState
import com.fazli.vispar.account.ApiException
import com.fazli.vispar.account.Device
import com.fazli.vispar.account.Profile
import com.fazli.vispar.account.SyncManager
import com.fazli.vispar.account.SyncStatus
import com.fazli.vispar.ui.glass.DialogAction
import com.fazli.vispar.ui.glass.DialogActionStyle
import com.fazli.vispar.ui.glass.DialogHeader
import com.fazli.vispar.ui.glass.Glass
import com.fazli.vispar.ui.glass.GlassAlertDialog
import com.fazli.vispar.ui.glass.GlassDialog
import com.fazli.vispar.ui.glass.GlassDivider
import com.fazli.vispar.ui.glass.GlassRow
import com.fazli.vispar.ui.glass.GlassScaffold
import com.fazli.vispar.ui.glass.GlassStyle
import com.fazli.vispar.ui.glass.GlassSurface
import com.fazli.vispar.ui.glass.GlassType
import com.fazli.vispar.ui.glass.LiquidButton
import com.fazli.vispar.ui.glass.LiquidIconButton
import com.fazli.vispar.ui.glass.LiquidLoader
import com.fazli.vispar.ui.glass.LocalBackdrop
import com.fazli.vispar.ui.glass.glass
import com.kyant.shapes.Capsule
import com.kyant.shapes.RoundedRectangle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Date

/** The blue of the verification tick (same on light and dark). */
val VerifiedBlue = Color(0xFF1D9BF0)

@Composable
fun AccountScreen(navController: NavController?) {
    val state by AccountManager.state.collectAsState()
    GlassScaffold(
        title = stringResource(R.string.account),
        onBack = { navController?.popBackStack() }
    ) { padding ->
        AnimatedContent(
            targetState = state is AccountState.SignedIn,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "account"
        ) { signedIn ->
            Column(
                Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = Glass.ScreenPadding)
                    .padding(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Column(
                    Modifier.widthIn(max = 520.dp).fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.spacedBy(18.dp)
                ) {
                    val profile = (state as? AccountState.SignedIn)?.profile
                    if (signedIn && profile != null) ProfileContent(profile) else LoginContent()
                }
            }
        }
    }
}

// ---- Sign in -------------------------------------------------------------------------------------

@Composable
private fun LoginContent() {
    val colors = Glass.colors
    val scope = rememberCoroutineScope()
    var phone by rememberSaveable { mutableStateOf("") }
    var code by rememberSaveable { mutableStateOf("") }
    var codeSent by rememberSaveable { mutableStateOf(false) }
    var busy by remember { mutableStateOf(false) }
    var error by remember { mutableStateOf<String?>(null) }
    var resendIn by rememberSaveable { mutableIntStateOf(0) }
    val context = LocalContext.current
    val codeFocus = remember { FocusRequester() }

    LaunchedEffect(codeSent, resendIn) {
        if (resendIn > 0) {
            delay(1000)
            resendIn--
        }
    }

    fun messageFor(e: Throwable): String = context.getString(
        when ((e as? ApiException)?.code) {
            "bad_phone" -> R.string.err_bad_phone
            "cooldown", "too_many_requests" -> R.string.err_too_many
            "sms_failed", "unavailable" -> R.string.err_sms_failed
            "wrong_code" -> R.string.err_wrong_code
            "code_expired" -> R.string.err_code_expired
            "too_many_attempts" -> R.string.err_too_many_attempts
            "blocked" -> R.string.err_blocked
            "network" -> R.string.err_network
            else -> R.string.err_generic
        }
    )

    fun sendCode() {
        val normalized = normalizePhone(phone)
        if (normalized == null) {
            error = context.getString(R.string.err_bad_phone)
            return
        }
        if (busy) return
        busy = true
        error = null
        scope.launch {
            try {
                resendIn = AccountManager.requestCode(normalized)
                phone = normalized
                codeSent = true
                code = ""
            } catch (e: Exception) {
                error = messageFor(e)
            }
            busy = false
        }
    }

    fun verify() {
        if (busy || code.length != 5) return
        busy = true
        error = null
        scope.launch {
            try {
                AccountManager.verifyCode(phone, code)
            } catch (e: Exception) {
                error = messageFor(e)
                code = ""
            }
            busy = false
        }
    }

    LaunchedEffect(codeSent) { if (codeSent) runCatching { codeFocus.requestFocus() } }

    Spacer(Modifier.height(8.dp))
    HeroIcon(if (codeSent) Icons.Rounded.Sms else Icons.Rounded.Person)
    Text(
        stringResource(if (codeSent) R.string.login_enter_code else R.string.login_title),
        style = GlassType.Title,
        color = colors.content,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )
    Text(
        if (codeSent) stringResource(R.string.login_code_sent_to, phone) else stringResource(R.string.login_subtitle),
        style = GlassType.Callout,
        color = colors.contentSecondary,
        textAlign = TextAlign.Center,
        modifier = Modifier.fillMaxWidth()
    )

    if (!codeSent) {
        GlassField(
            value = phone,
            onValueChange = { phone = it.filter { c -> c.isDigit() || c == '+' || c == ' ' }.take(16); error = null },
            placeholder = "09xx xxx xxxx",
            icon = Icons.Rounded.PhoneAndroid,
            keyboardType = KeyboardType.Phone,
            onDone = { sendCode() },
        )
        PrimaryButton(stringResource(R.string.login_send_code), busy, enabled = phone.isNotBlank()) { sendCode() }
    } else {
        GlassField(
            value = code,
            onValueChange = {
                code = it.map { c -> persianToAscii(c) }.filter { c -> c.isDigit() }.joinToString("").take(5)
                error = null
                if (code.length == 5) verify()
            },
            placeholder = "• • • • •",
            icon = Icons.Rounded.Lock,
            keyboardType = KeyboardType.NumberPassword,
            centered = true,
            focusRequester = codeFocus,
            onDone = { verify() },
        )
        PrimaryButton(stringResource(R.string.login_verify), busy, enabled = code.length == 5) { verify() }
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            TextAction(stringResource(R.string.login_change_number)) {
                codeSent = false
                code = ""
                error = null
            }
            if (resendIn > 0) {
                Text(
                    stringResource(R.string.login_resend_in, resendIn),
                    style = GlassType.Callout,
                    color = colors.contentTertiary,
                    modifier = Modifier.padding(8.dp)
                )
            } else {
                TextAction(stringResource(R.string.login_resend)) { sendCode() }
            }
        }
    }

    error?.let {
        Text(it, style = GlassType.Callout, color = colors.danger, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
    }

    if (!codeSent) {
        GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
            Column(Modifier.padding(vertical = 6.dp)) {
                GlassRow(Icons.Rounded.Favorite, stringResource(R.string.benefit_favorites), subtitle = stringResource(R.string.benefit_favorites_msg), iconTint = colors.heart, trailing = null)
                GlassDivider()
                GlassRow(Icons.Rounded.Tv, stringResource(R.string.benefit_tv), subtitle = stringResource(R.string.benefit_tv_msg), iconTint = Color(0xFF5E5CE6), trailing = null)
                GlassDivider()
                GlassRow(Icons.Rounded.Verified, stringResource(R.string.benefit_free), subtitle = stringResource(R.string.benefit_free_msg), iconTint = Color(0xFF34C759), trailing = null)
            }
        }
    }
}

// ---- Profile -------------------------------------------------------------------------------------

@Composable
private fun ProfileContent(profile: Profile) {
    val colors = Glass.colors
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val syncStatus by SyncManager.status.collectAsState()
    var devices by remember { mutableStateOf<List<Device>?>(null) }
    var editName by remember { mutableStateOf(false) }
    var confirmSignOut by remember { mutableStateOf(false) }
    var confirmDelete by remember { mutableStateOf(false) }
    var revoke by remember { mutableStateOf<Device?>(null) }
    var actionError by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(Unit) {
        AccountManager.reloadProfile()
        devices = runCatching { AccountManager.devices() }.getOrElse { emptyList() }
        SyncManager.schedule(context)
    }

    // Header: avatar, name with the blue tick, phone, member since.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(
            Modifier.fillMaxWidth().padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Avatar(profile)
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    profile.displayName.ifBlank { stringResource(R.string.profile_no_name) },
                    style = GlassType.Title,
                    color = if (profile.displayName.isBlank()) colors.contentSecondary else colors.content
                )
                if (profile.verified) {
                    Icon(Icons.Rounded.Verified, stringResource(R.string.verified), tint = VerifiedBlue, modifier = Modifier.size(24.dp))
                }
            }
            CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                Text(profile.phone, style = GlassType.Callout, color = colors.contentSecondary)
            }
            memberSince(profile.createdAt)?.let {
                Text(stringResource(R.string.member_since, it), style = GlassType.Caption, color = colors.contentTertiary)
            }
            if (profile.verified) {
                Row(
                    Modifier
                        .padding(top = 6.dp)
                        .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = VerifiedBlue.copy(alpha = 0.18f), shadow = null)
                        .padding(horizontal = 12.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(Icons.Rounded.Verified, null, tint = VerifiedBlue, modifier = Modifier.size(16.dp))
                    Text(stringResource(R.string.verified_account), style = GlassType.Caption, color = colors.content)
                }
            }
            Spacer(Modifier.height(6.dp))
            LiquidButton(onClick = { editName = true }, height = 42.dp) {
                Icon(Icons.Rounded.Edit, null, tint = colors.content, modifier = Modifier.size(18.dp))
                Text(stringResource(R.string.edit_name), style = GlassType.Headline, color = colors.content)
            }
        }
    }

    // Sync.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        GlassRow(
            icon = Icons.Rounded.CloudSync,
            title = stringResource(R.string.sync_title),
            subtitle = when (val s = syncStatus) {
                SyncStatus.Syncing -> stringResource(R.string.sync_running)
                is SyncStatus.Failed -> stringResource(R.string.sync_failed)
                is SyncStatus.Done -> stringResource(R.string.sync_last, relativeTime(s.atMs))
                SyncStatus.Idle -> SyncManager.lastSyncMs(context).takeIf { it > 0 }
                    ?.let { stringResource(R.string.sync_last, relativeTime(it)) } ?: stringResource(R.string.sync_msg)
            },
            iconTint = Color(0xFF0A84FF),
            onClick = { scope.launch { SyncManager.syncNow(context) } },
            trailing = {
                if (syncStatus == SyncStatus.Syncing) LiquidLoader(size = 26.dp)
                else Icon(Icons.Rounded.Sync, null, tint = colors.contentSecondary, modifier = Modifier.size(22.dp))
            }
        )
    }

    // Devices.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(Modifier.padding(vertical = 6.dp)) {
            Row(Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Rounded.Devices, null, tint = colors.contentSecondary, modifier = Modifier.size(18.dp))
                Spacer(Modifier.size(8.dp))
                Text(stringResource(R.string.devices), style = GlassType.Headline, color = colors.content)
            }
            val list = devices
            when {
                list == null -> Box(Modifier.fillMaxWidth().padding(16.dp), contentAlignment = Alignment.Center) { LiquidLoader(size = 36.dp) }
                list.isEmpty() -> Text(stringResource(R.string.devices_none), style = GlassType.Callout, color = colors.contentSecondary, modifier = Modifier.padding(16.dp))
                else -> list.forEachIndexed { i, d ->
                    if (i > 0) GlassDivider()
                    GlassRow(
                        icon = when (d.kind) { "tv" -> Icons.Rounded.Tv; "tablet" -> Icons.Rounded.Tablet; else -> Icons.Rounded.PhoneAndroid },
                        title = d.name.ifBlank { stringResource(R.string.device_unknown) },
                        subtitle = if (d.current) stringResource(R.string.device_this) else
                            stringResource(R.string.device_last_seen, relativeTime(SyncManager.parseIso(d.lastSeenAt) ?: 0)),
                        iconTint = if (d.current) Color(0xFF34C759) else Color(0xFF8E8E93),
                        trailing = if (d.current) null else {
                            {
                                LiquidIconButton(
                                    onClick = { revoke = d },
                                    icon = Icons.Rounded.Close,
                                    contentDescription = stringResource(R.string.device_sign_out),
                                    size = 36.dp,
                                    iconSize = 18.dp
                                )
                            }
                        }
                    )
                }
            }
        }
    }

    // Sign out / delete.
    GlassSurface(Modifier.fillMaxWidth(), shape = RoundedRectangle(Glass.CardRadius)) {
        Column(Modifier.padding(vertical = 6.dp)) {
            GlassRow(
                icon = Icons.AutoMirrored.Rounded.Logout,
                title = stringResource(R.string.sign_out),
                subtitle = stringResource(R.string.sign_out_msg),
                iconTint = Color(0xFFFF9F0A),
                onClick = { confirmSignOut = true },
                trailing = null
            )
            GlassDivider()
            GlassRow(
                icon = Icons.Rounded.DeleteForever,
                title = stringResource(R.string.delete_account),
                subtitle = stringResource(R.string.delete_account_msg),
                iconTint = colors.danger,
                onClick = { confirmDelete = true },
                trailing = null
            )
        }
    }
    actionError?.let { Text(it, style = GlassType.Callout, color = colors.danger, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()) }

    if (editName) {
        var name by remember { mutableStateOf(profile.displayName) }
        var saving by remember { mutableStateOf(false) }
        GlassDialog(onDismissRequest = { editName = false }) {
            DialogHeader(stringResource(R.string.edit_name), stringResource(R.string.edit_name_msg), Icons.Rounded.Person)
            GlassField(value = name, onValueChange = { name = it.take(40) }, placeholder = stringResource(R.string.name_placeholder), icon = Icons.Rounded.Edit, keyboardType = KeyboardType.Text)
            DialogAction(stringResource(R.string.save), onClick = {
                if (!saving) {
                    saving = true
                    scope.launch {
                        runCatching { AccountManager.updateName(name) }
                            .onFailure { actionError = context.getString(R.string.err_generic) }
                        saving = false
                        editName = false
                    }
                }
            }, style = DialogActionStyle.Primary)
            DialogAction(stringResource(R.string.cancel), onClick = { editName = false }, style = DialogActionStyle.Plain)
        }
    }
    revoke?.let { d ->
        GlassAlertDialog(
            title = stringResource(R.string.device_sign_out),
            message = stringResource(R.string.device_sign_out_msg, d.name),
            confirmText = stringResource(R.string.device_sign_out),
            onConfirm = {
                revoke = null
                scope.launch {
                    runCatching { AccountManager.revokeDevice(d.id) }
                        .onSuccess { devices = devices?.filterNot { it.id == d.id } }
                        .onFailure { actionError = context.getString(R.string.err_generic) }
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { revoke = null },
            icon = Icons.Rounded.Devices,
            destructive = true
        )
    }
    if (confirmSignOut) {
        GlassAlertDialog(
            title = stringResource(R.string.sign_out),
            message = stringResource(R.string.sign_out_confirm),
            confirmText = stringResource(R.string.sign_out),
            onConfirm = {
                confirmSignOut = false
                scope.launch {
                    SyncManager.syncNow(context)
                    AccountManager.signOut()
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmSignOut = false },
            icon = Icons.AutoMirrored.Rounded.Logout,
        )
    }
    if (confirmDelete) {
        GlassAlertDialog(
            title = stringResource(R.string.delete_account),
            message = stringResource(R.string.delete_account_confirm),
            confirmText = stringResource(R.string.delete_account),
            onConfirm = {
                confirmDelete = false
                scope.launch {
                    runCatching { AccountManager.deleteAccount() }
                        .onFailure { actionError = context.getString(R.string.err_generic) }
                }
            },
            dismissText = stringResource(R.string.cancel),
            onDismiss = { confirmDelete = false },
            icon = Icons.Rounded.DeleteForever,
            destructive = true
        )
    }
}

// ---- Pieces --------------------------------------------------------------------------------------

@Composable
fun Avatar(profile: Profile?, size: androidx.compose.ui.unit.Dp = 88.dp) {
    val colors = Glass.colors
    Box(
        Modifier
            .size(size)
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Clear, tint = colors.accent.copy(alpha = 0.20f), shadow = null),
        contentAlignment = Alignment.Center
    ) {
        val initial = profile?.displayName?.trim()?.firstOrNull()?.toString()
        if (initial != null) {
            Text(initial.uppercase(), style = GlassType.LargeTitle.copy(fontSize = (size.value * 0.42f).sp), color = colors.content)
        } else {
            Icon(Icons.Rounded.Person, null, tint = colors.contentSecondary, modifier = Modifier.size(size * 0.5f))
        }
    }
}

@Composable
private fun HeroIcon(icon: ImageVector) {
    val colors = Glass.colors
    Box(
        Modifier
            .size(96.dp)
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle.Floating, tint = colors.glassTint),
        contentAlignment = Alignment.Center
    ) {
        Icon(icon, null, tint = colors.content, modifier = Modifier.size(44.dp))
    }
}

@Composable
private fun GlassField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    icon: ImageVector,
    keyboardType: KeyboardType,
    centered: Boolean = false,
    focusRequester: FocusRequester = remember { FocusRequester() },
    onDone: () -> Unit = {},
) {
    val colors = Glass.colors
    val ltr = keyboardType != KeyboardType.Text
    Row(
        Modifier
            .fillMaxWidth()
            .height(58.dp)
            .glass(LocalBackdrop.current, Capsule(), colors, GlassStyle(blur = 10.dp, refractionHeight = 14.dp, refractionAmount = 28.dp))
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Icon(icon, null, tint = colors.contentSecondary, modifier = Modifier.size(22.dp))
        // Numbers are always typed left to right, even in the Persian UI.
        CompositionLocalProvider(LocalLayoutDirection provides if (ltr) LayoutDirection.Ltr else LocalLayoutDirection.current) {
            Box(Modifier.weight(1f), contentAlignment = if (centered) Alignment.Center else Alignment.CenterStart) {
                val style = (if (centered) GlassType.Title2.copy(letterSpacing = 8.sp) else GlassType.Headline)
                    .copy(color = colors.content, textAlign = if (centered) TextAlign.Center else TextAlign.Start)
                if (value.isEmpty()) {
                    Text(placeholder, style = style.copy(color = colors.contentTertiary), maxLines = 1, modifier = Modifier.fillMaxWidth())
                }
                BasicTextField(
                    value = value,
                    onValueChange = onValueChange,
                    singleLine = true,
                    textStyle = style,
                    cursorBrush = SolidColor(colors.accent),
                    keyboardOptions = KeyboardOptions(keyboardType = keyboardType, imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { onDone() }),
                    modifier = Modifier.fillMaxWidth().focusRequester(focusRequester)
                )
            }
        }
    }
}

@Composable
private fun PrimaryButton(text: String, busy: Boolean, enabled: Boolean, onClick: () -> Unit) {
    val colors = Glass.colors
    LiquidButton(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        tint = colors.accent,
        height = 54.dp,
        enabled = enabled && !busy,
    ) {
        if (busy) LiquidLoader(size = 28.dp)
        else Text(text, style = GlassType.Headline, color = colors.onAccent)
    }
}

@Composable
private fun TextAction(text: String, onClick: () -> Unit) {
    LiquidButton(onClick = onClick, height = 40.dp, surfaceColor = Color.Transparent) {
        Text(text, style = GlassType.Callout, color = Glass.colors.accent)
    }
}

// ---- Helpers -------------------------------------------------------------------------------------

private fun persianToAscii(c: Char): Char = when (c) {
    in '۰'..'۹' -> '0' + (c - '۰')
    in '٠'..'٩' -> '0' + (c - '٠')
    else -> c
}

/** Same rules as the server: +98…, 0098…, 98…, 9…, Persian digits → 09XXXXXXXXX, or null. */
fun normalizePhone(input: String): String? {
    var d = input.map { persianToAscii(it) }.filter { it.isDigit() }.joinToString("")
    when {
        d.startsWith("0098") -> d = "0" + d.drop(4)
        d.startsWith("98") && d.length == 12 -> d = "0" + d.drop(2)
        d.startsWith("9") && d.length == 10 -> d = "0$d"
    }
    return d.takeIf { it.length == 11 && it.startsWith("09") }
}

@Composable
private fun memberSince(iso: String): String? {
    val ms = SyncManager.parseIso(iso) ?: return null
    val locale = LocalConfiguration.current.locales[0]
    val uLocale = if (locale.language == "fa") ULocale("fa_IR@calendar=persian") else ULocale.forLocale(locale)
    return runCatching { DateFormat.getDateInstance(DateFormat.LONG, uLocale).format(Date(ms)) }.getOrNull()
}

@Composable
private fun relativeTime(ms: Long): String {
    val diff = (System.currentTimeMillis() - ms).coerceAtLeast(0)
    val minutes = diff / 60_000
    return when {
        ms <= 0 -> "—"
        minutes < 1 -> stringResource(R.string.time_just_now)
        minutes < 60 -> stringResource(R.string.time_minutes_ago, minutes.toInt())
        minutes < 60 * 24 -> stringResource(R.string.time_hours_ago, (minutes / 60).toInt())
        else -> stringResource(R.string.time_days_ago, (minutes / 60 / 24).toInt())
    }
}
