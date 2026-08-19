package com.maxxcodebug.maxxequalizer.ui

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import com.maxxcodebug.maxxequalizer.R

object AmoledThemeHelper {
    private const val PREFS = "maxxeq_amoled_prefs"
    private const val KEY_AMOLED = "amoled_enabled"

    fun isEnabled(context: Context): Boolean =
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getBoolean(KEY_AMOLED, false)

    fun setEnabled(context: Context, enabled: Boolean) {
        context.applicationContext
            .getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit()
            .putBoolean(KEY_AMOLED, enabled)
            .apply()
    }

    fun applyIfNeeded(activity: Activity) {
        val isNight = (activity.resources.configuration.uiMode and
            Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES
        if (isNight && isEnabled(activity)) {
            activity.theme.applyStyle(R.style.ThemeOverlay_MaxxEqualizer_Amoled, true)
        }
    }
}
