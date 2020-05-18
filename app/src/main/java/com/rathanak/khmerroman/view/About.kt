package com.rathanak.khmerroman.view

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.rathanak.khmerroman.R

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
