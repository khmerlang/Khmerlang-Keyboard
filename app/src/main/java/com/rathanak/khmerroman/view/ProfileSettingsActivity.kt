package com.rathanak.khmerroman.view

import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceFragmentCompat
import com.rathanak.khmerroman.R
import com.rathanak.khmerroman.data.DataLoader
import kotlinx.android.synthetic.main.settings_activity.*

class ProfileSettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(
                R.id.settings,
                SettingsFragment()
            )
            .commit()
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        btnReset.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle(R.string.dialogConfirmTitle)
            builder.setMessage(R.string.dialogConfirmMessage)
            builder.setIcon(android.R.drawable.ic_dialog_alert)
            builder.setPositiveButton("Yes"){dialogInterface, which ->
                Toast.makeText(applicationContext,"reseting data",Toast.LENGTH_LONG).show()
                var dataLoader = DataLoader(this)
                dataLoader.reInitRomanData()
            }
            builder.setNeutralButton("Cancel"){dialogInterface , which ->
                Toast.makeText(applicationContext,"operation cancel",Toast.LENGTH_LONG).show()
            }
            val alertDialog: AlertDialog = builder.create()
            alertDialog.setCancelable(false)
            alertDialog.show()
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

    class SettingsFragment : PreferenceFragmentCompat() {
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)
        }
    }

//    private fun setupActions() {
//        binding.enableVibration.apply {
//            isChecked = preferences.getBoolean(KEY_ENABLE_VIBRATION)
//            setOnClickListener {
//                isChecked = !isChecked
//                preferences.putBoolean(KEY_ENABLE_VIBRATION, isChecked)
//                preferences.putBoolean(KEY_NEEDS_RELOAD, true)
//            }
//        }
//
//        binding.enableSound.apply {
//            isChecked = preferences.getBoolean(KEY_ENABLE_SOUND)
//            setOnClickListener {
//                isChecked = !isChecked
//                preferences.putBoolean(KEY_ENABLE_SOUND, isChecked)
//                preferences.putBoolean(KEY_NEEDS_RELOAD, true)
//            }
//        }
//
//        binding.email.setOnClickListener {
//            val emailIntent = Intent(Intent.ACTION_SEND)
//
//            emailIntent.data = Uri.parse("mailto:")
//            emailIntent.type = "text/plain"
//            emailIntent.putExtra(Intent.EXTRA_EMAIL, arrayOf(email))
//            emailIntent.putExtra(Intent.EXTRA_SUBJECT, "[Lisu Keyboard Android] Feedback")
//
//            try {
//                startActivity(Intent.createChooser(emailIntent, "Send feedback..."))
//            } catch (ex: android.content.ActivityNotFoundException) {
//                Toast.makeText(this, "There is no email client installed.", Toast.LENGTH_SHORT).show()
//            }
//        }
//    }

}