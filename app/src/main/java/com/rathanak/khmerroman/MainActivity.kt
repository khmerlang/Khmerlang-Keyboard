package com.rathanak.khmerroman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import kotlin.system.exitProcess
import com.rathanak.khmerroman.R

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnProfile = findViewById(R.id.btn_profile) as Button
        val btnCustomRoman = findViewById(R.id.btn_custom_roman) as Button
        val btnRoman = findViewById(R.id.btn_roman) as Button
        val btnAbout = findViewById(R.id.btn_about) as Button
        val btnClose = findViewById(R.id.btn_close) as Button
        btnProfile.setOnClickListener {
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
}
