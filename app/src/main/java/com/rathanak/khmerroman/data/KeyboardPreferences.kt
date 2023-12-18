package com.rathanak.khmerroman.data

import android.content.Context
import android.content.SharedPreferences

class KeyboardPreferences(context: Context) {
    private val KEYBOARD_PREFERENCES_NAME = "r2k_keyboard_preferences"

    var preference: SharedPreferences =
        context.getSharedPreferences(KEYBOARD_PREFERENCES_NAME, Context.MODE_PRIVATE)

    fun putBoolean(key: String, data: Boolean) {
        preference.edit()?.putBoolean(key, data)?.apply()
    }

    fun putInt(key: String, data: Int) {
        preference.edit()?.putInt(key, data)?.apply()
    }

    fun putString(key: String, data: String) {
        preference.edit()?.putString(key, data)?.apply()
    }

    fun getBoolean(key: String, defaultVale: Boolean = false): Boolean {
        return preference.getBoolean(key, defaultVale)
    }

    fun getInt(key: String, defaultInt: Int): Int {
        return preference.getInt(key, defaultInt)
    }

    fun getString(key: String, defaultInt: String): String? {
        return preference.getString(key, defaultInt)
    }

    companion object {
        const val STATUS_NONE = 0
        const val STATUS_DOWNLOADING = 10
        const val STATUS_DOWNLOAD_FAIL = 100
        const val STATUS_DOWNLOADED = 200

        const val KEY_CURRENT_LANGUAGE_IDX = "key_current_language_idx"
        const val KEY_ENABLE_VIBRATION = "key_enable_vibration"
        const val KEY_ENABLE_SOUND = "key_enable_sound"
        const val KEY_ENABLE_DARK_MOOD = "key_enable_dark_mood"
        const val KEY_RM_CORRECTION_MODE = "key_rm_correction_mode"
        const val KEY_EN_CORRECTION_MODE = "key_en_correction_mode"
        const val KEY_NEEDS_RELOAD = "key_needs_reload"
        const val KEY_SHOW_KEY_LABEL_VIEW = "key_show_keyboard_label"
        const val KEY_DATA_STATUS = "key_data_status"
        const val KEY_AUTO_TYPING_CORRECTION_MODE = "key_auto_typing_suggestion"
    }
}