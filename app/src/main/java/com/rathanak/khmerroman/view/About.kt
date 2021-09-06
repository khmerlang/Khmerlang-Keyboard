package com.rathanak.khmerroman.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.rathanak.khmerroman.R
import kotlinx.android.synthetic.main.activity_about.*
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.activity_main.txtKhmerlang

class About : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        btnBack.setOnClickListener {
            // Go back
            super.onBackPressed()
        }

        btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:sreangrathanak@yahoo.com"))
            startActivity(intent)
        }

        btnFb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.facebook.com/khmerlang.official"))
            startActivity(intent)
        }

        btnGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khmerlang"))
            startActivity(intent)
        }

        btnWeb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.khmerlang.com"))
            startActivity(intent)
        }

        txtKhmerlang.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.khmerlang.com"))
            startActivity(intent)
        }
    }
}
