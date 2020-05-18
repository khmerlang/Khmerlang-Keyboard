package com.rathanak.khmerroman.view

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import com.rathanak.khmerroman.*
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.data.KeyboardPreferences
import com.rathanak.khmerroman.data.KeyboardPreferences.Companion.KEY_NOT_FIRST_RUN
import com.rathanak.khmerroman.view.dialog.EnableKeyboardDialog
import io.realm.Realm
import kotlinx.android.synthetic.main.activity_main.*
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm
//    private lateinit var binding: ActivitySettingBinding
    private lateinit var preferences: KeyboardPreferences
    private var enabledKeyboard: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_setting)
        preferences = KeyboardPreferences(applicationContext)

        setupActions()
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        // check is first open app
        if (!preferences.getBoolean(KEY_NOT_FIRST_RUN)) {
            // load data to realm db
            var dataLoader = DataLoader(this)
            dataLoader.reInitRomanData()
            preferences.putBoolean(KEY_NOT_FIRST_RUN, true)
        }

        setContentView(R.layout.activity_main)

        btnSetting.setOnClickListener {
            // Open my account activity
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
        btnCustomRoman.setOnClickListener {
            // Open Custom Roman mapping fragment
            val intent = Intent(this, CustomMapping::class.java)
            startActivity(intent)
        }
        btnRoman.setOnClickListener {
            // Open roman mapping fragment
            val intent = Intent(this, RomanMapping::class.java)
            startActivity(intent)
        }
        btnAbout.setOnClickListener {
            // Open about fragment
            val intent = Intent(this, About::class.java)
            startActivity(intent)
        }
        btnClose.setOnClickListener {
            moveTaskToBack(true);
            exitProcess(-1)
        }
    }

    override fun onResume() {
        super.onResume()
        checkKeyboardEnabled()
    }

    private fun checkKeyboardEnabled() {
        (getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager).let {
            enabledKeyboard = it.enabledInputMethodList.any { it.packageName == packageName }
            val dialogShown = supportFragmentManager.findFragmentByTag(EnableKeyboardDialog.TAG) != null
            if (!enabledKeyboard && !dialogShown) {
                // Show setting dialog
                showEnableKeyboardDialog()
            }

            // Check if the keyboard has been picked
            val imeSelected = Settings.Secure.getString(
                contentResolver,
                Settings.Secure.DEFAULT_INPUT_METHOD
            ).contains(packageName)

            if (enabledKeyboard && !imeSelected) {
                Handler().postDelayed({ it.showInputMethodPicker() }, 500)
            }
        }
    }

    private fun showEnableKeyboardDialog() {
        val dialog =  EnableKeyboardDialog()
        dialog.show(supportFragmentManager, EnableKeyboardDialog.TAG)
    }

    private fun setupActions() {
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
    }

    companion object {
        const val email = "gwamosi@gmail.com"
    }
}
