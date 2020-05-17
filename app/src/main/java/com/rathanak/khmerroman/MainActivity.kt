package com.rathanak.khmerroman

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.rathanak.khmerroman.data.DataLoader
import com.rathanak.khmerroman.migration.MyMigration
import io.realm.Realm
import io.realm.RealmConfiguration
import kotlinx.android.synthetic.main.activity_main.*
import java.io.FileNotFoundException
import kotlin.system.exitProcess


class MainActivity : AppCompatActivity() {
    private lateinit var realm: Realm

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Realm.init(this)
        realm = Realm.getDefaultInstance()
        val prefs = this.getSharedPreferences("com.rathanak.khmerroman", Context.MODE_PRIVATE);

        // check is first open app
        if (prefs.getBoolean("firstrun", true)) {
            // load data to realm db
            var dataLoader = DataLoader(this)
            dataLoader.reInitRomanData()
            prefs.edit().putBoolean("firstrun", false).commit()
        }

        setContentView(R.layout.activity_main)

        btnSetting.setOnClickListener {
            // Open my account activity
            val intent = Intent(this, ProfileSettingsActivity::class.java)
            startActivity(intent)
        }
        btnCustomRoman.setOnClickListener {
            // Open Custom Roman mapping fragment
            val intent = Intent(this, CustomMapping ::class.java)
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
}
