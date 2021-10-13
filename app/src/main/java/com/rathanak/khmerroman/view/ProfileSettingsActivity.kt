package com.rathanak.khmerroman.view

import android.app.AlertDialog
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreferenceCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.KeyboardPreferences
import kotlinx.android.synthetic.main.settings_activity.*
import android.content.DialogInterface
import android.view.View
import android.widget.Toast
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.keyboard.R2KhmerService
import com.rathanak.khmerroman.utils.DownloadData
import kotlinx.android.synthetic.main.activity_roman_mapping.*


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

        btnReset.setOnClickListener {
            buildResetDialog()
        }

        btnReDownload.setOnClickListener {
            buildReDownloadDialog()
        }

        updateVisibility()
        listenJobDone()
    }

    private fun buildReDownloadDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.dialogConfirmDownloadTitle)
        builder.setMessage(R.string.dialogConfirmDownloadMessage)
            .setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { dialog, id ->
                    btnReDownload.visibility = View.GONE
                    btnReDownloading.visibility = View.VISIBLE
                    R2KhmerService.downloadDataPrevStatus = R2KhmerService.downloadDataStatus
                    R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_DOWNLOADING
                    val download = DownloadData()
                    download.downloadKeyboardData()
                    listenJobDone()
                })
            .setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { dialog, id ->
                    // User cancelled the dialog
                })
        builder.show()
    }
    private fun buildResetDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setCancelable(true)
        builder.setTitle(R.string.dialogConfirmTitle)
        builder.setMessage(R.string.dialogConfirmMessage)
            .setPositiveButton(R.string.ok,
                DialogInterface.OnClickListener { _, _ ->
                    val dataAdapter = DataLoader()
                    dataAdapter.clearDBData(true)
                    R2KhmerService.downloadDataStatus = KeyboardPreferences.STATUS_NONE
                    KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, KeyboardPreferences.STATUS_NONE)
                })
            .setNegativeButton(R.string.cancel,
                DialogInterface.OnClickListener { _, _ ->
                    // User cancelled the dialog
                })
        builder.show()
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

    private fun listenJobDone() {
        if (R2KhmerService.jobLoadData != null) {
            R2KhmerService.jobLoadData!!.invokeOnCompletion {
                btnReDownloading.visibility = View.GONE
                btnReDownload.visibility = View.VISIBLE
                if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOAD_FAIL) {
                    Toast.makeText(applicationContext, R.string.download_fail, Toast.LENGTH_LONG).show()
                    R2KhmerService.downloadDataStatus = R2KhmerService.downloadDataPrevStatus
                    KhmerLangApp.preferences?.putInt(KeyboardPreferences.KEY_DATA_STATUS, R2KhmerService.downloadDataPrevStatus)
                }
            }
        }
    }

    private fun updateVisibility() {
        btnReDownload.visibility = View.GONE
        btnReDownloading.visibility = View.GONE
        if (R2KhmerService.downloadDataStatus == KeyboardPreferences.STATUS_DOWNLOADING) {
            btnReDownloading.visibility = View.VISIBLE
        } else {
            btnReDownload.visibility = View.VISIBLE
        }
    }
}