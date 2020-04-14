package com.rathanak.khmerroman.keyboard

import android.content.Intent
import android.os.Bundle
import android.preference.PreferenceActivity
import com.rathanak.khmerroman.R


/**
 * Displays the IME preferences inside the input method setting.
 */
class ImePreferences : PreferenceActivity() {
    override fun getIntent(): Intent {
        val modIntent = Intent(super.getIntent())
        modIntent.putExtra(PreferenceActivity.EXTRA_NO_HEADERS, true)
        return modIntent
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // We overwrite the title of the activity, as the default one is "Voice Search".
        setTitle(R.string.settings_name)
    }

    override fun isValidFragment(fragmentName: String): Boolean {
        return super.isValidFragment(fragmentName)
    }
}