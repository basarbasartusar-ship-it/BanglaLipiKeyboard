package com.banglakb.keyboard.util

import android.content.Context
import android.content.SharedPreferences

enum class AppTheme { LIGHT, DARK, AMOLED }

class Prefs(context: Context) {
    private val sp: SharedPreferences =
        context.getSharedPreferences("bangla_keyboard_prefs", Context.MODE_PRIVATE)

    var currentLayoutId: String
        get() = sp.getString(KEY_LAYOUT, "QWERTY_EN") ?: "QWERTY_EN"
        set(value) = sp.edit().putString(KEY_LAYOUT, value).apply()

    var theme: AppTheme
        get() = AppTheme.valueOf(sp.getString(KEY_THEME, AppTheme.LIGHT.name) ?: AppTheme.LIGHT.name)
        set(value) = sp.edit().putString(KEY_THEME, value.name).apply()

    var soundEnabled: Boolean
        get() = sp.getBoolean(KEY_SOUND, false)
        set(value) = sp.edit().putBoolean(KEY_SOUND, value).apply()

    var hapticEnabled: Boolean
        get() = sp.getBoolean(KEY_HAPTIC, true)
        set(value) = sp.edit().putBoolean(KEY_HAPTIC, value).apply()

    var keyPopupEnabled: Boolean
        get() = sp.getBoolean(KEY_POPUP, true)
        set(value) = sp.edit().putBoolean(KEY_POPUP, value).apply()

    var autoCapitalize: Boolean
        get() = sp.getBoolean(KEY_AUTO_CAP, true)
        set(value) = sp.edit().putBoolean(KEY_AUTO_CAP, value).apply()

    var doubleSpacePeriod: Boolean
        get() = sp.getBoolean(KEY_DOUBLE_SPACE, true)
        set(value) = sp.edit().putBoolean(KEY_DOUBLE_SPACE, value).apply()

    var wordSuggestions: Boolean
        get() = sp.getBoolean(KEY_SUGGESTIONS, true)
        set(value) = sp.edit().putBoolean(KEY_SUGGESTIONS, value).apply()

    var keyboardHeightPercent: Int
        get() = sp.getInt(KEY_HEIGHT, 100)
        set(value) = sp.edit().putInt(KEY_HEIGHT, value).apply()

    var oneHandedMode: Boolean
        get() = sp.getBoolean(KEY_ONE_HANDED, false)
        set(value) = sp.edit().putBoolean(KEY_ONE_HANDED, value).apply()

    companion object {
        private const val KEY_LAYOUT = "current_layout"
        private const val KEY_THEME = "theme"
        private const val KEY_SOUND = "sound_enabled"
        private const val KEY_HAPTIC = "haptic_enabled"
        private const val KEY_POPUP = "key_popup_enabled"
        private const val KEY_AUTO_CAP = "auto_capitalize"
        private const val KEY_DOUBLE_SPACE = "double_space_period"
        private const val KEY_SUGGESTIONS = "word_suggestions"
        private const val KEY_HEIGHT = "keyboard_height_pct"
        private const val KEY_ONE_HANDED = "one_handed_mode"
    }
}
