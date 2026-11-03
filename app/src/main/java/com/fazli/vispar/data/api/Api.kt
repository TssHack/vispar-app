package com.fazli.vispar.data.api

import android.content.Context
import com.fazli.vispar.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.net.URLEncoder
import java.util.concurrent.TimeUnit

/**
 * Everything goes through the Vispar server (https://vispar.zarrinpcshop.com/v1), which proxies and
 * caches the content API. The app no longer holds any content API key.
 */
object Api {
    const val BASE = BuildConfig.API_BASE

    val json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
        encodeDefaults = true
    }

    @Volatile
    private var cache: Cache? = null

    /** Gives the shared client a small HTTP cache so catalog pages respect Cache-Control. */
    fun init(context: Context) {
        if (cache == null) cache = Cache(File(context.cacheDir, "http"), 10L * 1024 * 1024)
    }

    val client: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(20, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .retryOnConnectionFailure(true)
            .apply { cache?.let { cache(it) } }
            .addInterceptor { chain ->
                chain.proceed(
                    chain.request().newBuilder()
                        .header("User-Agent", "Vispar-Android/${BuildConfig.VERSION_NAME}")
                        .build()
                )
            }
            .build()
    }

    fun movies(page: Int) = "$BASE/catalog/movies?page=$page"
    fun series(page: Int) = "$BASE/catalog/series?page=$page"
    fun seasons(seriesId: Int) = "$BASE/catalog/series/$seriesId/seasons"
    fun search(query: String) = "$BASE/catalog/search?q=" + URLEncoder.encode(query, "UTF-8")
}
