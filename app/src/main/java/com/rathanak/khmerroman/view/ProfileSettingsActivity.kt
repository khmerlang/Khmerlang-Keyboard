package com.rathanak.khmerroman.view

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences

class ProfileSettingsActivity : AppCompatActivity() {
    private lateinit var preferences: KeyboardPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        preferences = KeyboardPreferences(applicationContext)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment(preferences)
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    class SettingsFragment(private val preferences: KeyboardPreferences) : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
            val enableVibration: SwitchPreferenceCompat? = findPreference(KeyboardPreferences.KEY_ENABLE_VIBRATION)
            enableVibration?.setOnPreferenceChangeListener{ _, newValue ->
                preferences.putBoolean(KeyboardPreferences.KEY_ENABLE_VIBRATION,
                    newValue as Boolean
                )
                preferences.putBoolean(KeyboardPreferences.KEY_NEEDS_RELOAD, true)
                true
            }
            val enableSound: SwitchPreferenceCompat? = findPreference(KeyboardPreferences.KEY_ENABLE_SOUND)
            enableSound?.setOnPreferenceChangeListener{ _, newValue ->
                preferences.putBoolean(KeyboardPreferences.KEY_ENABLE_SOUND, newValue as Boolean)
                preferences.putBoolean(KeyboardPreferences.KEY_NEEDS_RELOAD, true)
                true
            }
            val switchDarkMode: SwitchPreferenceCompat? = findPreference(KeyboardPreferences.KEY_ENABLE_DARK_MOOD)
            switchDarkMode?.setOnPreferenceChangeListener{ _, newValue ->
                preferences.putBoolean(KeyboardPreferences.KEY_ENABLE_DARK_MOOD, newValue as Boolean)
                preferences.putBoolean(KeyboardPreferences.KEY_NEEDS_RELOAD, true)
                true
            }
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                finish()
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }
}