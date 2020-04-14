package com.rathanak.khmerroman

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        val btnBack = findViewById(R.id.btn_back) as Button
        btnBack.setOnClickListener {
            // Go back
            super.onBackPressed()
        }
    }
}
