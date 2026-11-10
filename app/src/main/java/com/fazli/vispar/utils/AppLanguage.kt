package com.fazli.vispar.utils

import android.content.Context
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.edit
import androidx.core.os.LocaleListCompat

/** In-app language: Persian, English or whatever the system uses. */
enum class AppLanguage(val tag: String) {
    Persian("fa"),
    English("en"),
    System("");

    companion object {
        fun current(): AppLanguage {
            val tags = AppCompatDelegate.getApplicationLocales().toLanguageTags()
            return when {
                tags.startsWith("fa") -> Persian
                tags.startsWith("en") -> English
                else -> System
            }
        }

        /** Switches the app language; activities are recreated with the new locale and direction. */
        fun apply(language: AppLanguage) {
            AppCompatDelegate.setApplicationLocales(
                if (language == System) LocaleListCompat.getEmptyLocaleList()
                else LocaleListCompat.forLanguageTags(language.tag)
            )
        }

        /**
         * Vispar has always been Persian: on the very first launch pick Persian explicitly, even on
         * phones whose system language is English. Afterwards the user's choice is kept.
         */
        fun ensureInitialized(context: Context) {
            val prefs = context.getSharedPreferences("app_language", Context.MODE_PRIVATE)
            if (prefs.getBoolean("initialized", false)) return
            prefs.edit { putBoolean("initialized", true) }
            if (AppCompatDelegate.getApplicationLocales().isEmpty) apply(Persian)
        }
    }
}
