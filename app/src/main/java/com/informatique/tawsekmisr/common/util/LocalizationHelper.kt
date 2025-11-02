package com.informatique.tawsekmisr.common.util

import android.content.Context
import androidx.annotation.StringRes
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.*
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Localization Helper for non-Composable contexts
 * Uses the app's current locale to provide localized strings
 */
@Singleton
class LocalizationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {

    /**
     * Get localized string using the current app locale
     */
    fun getString(@StringRes resId: Int): String {
        val locale = getCurrentLocale()
        val config = context.resources.configuration.apply {
            setLocale(locale)
        }
        val localizedContext = context.createConfigurationContext(config)
        return localizedContext.resources.getString(resId)
    }

    /**
     * Get the current app locale from DataStore/SharedPreferences
     * This should match the locale used by LocalAppLocale in Compose
     */
    private fun getCurrentLocale(): Locale {
        // Get the locale from SharedPreferences (same as used by LocalAppLocale)
        val sharedPrefs = context.getSharedPreferences("app_preferences", Context.MODE_PRIVATE)
        val languageCode = sharedPrefs.getString("app_language", "ar") ?: "ar"
        return Locale(languageCode)
    }
}

