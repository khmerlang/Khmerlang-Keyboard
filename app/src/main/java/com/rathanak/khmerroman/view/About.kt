package com.rathanak.khmerroman.view

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.rathanak.khmerroman.databinding.ActivityAboutBinding

class About : AppCompatActivity() {

    private lateinit var binding: ActivityAboutBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAboutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener {
            // Go back
            super.onBackPressed()
        }

        binding.btnEmail.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("mailto:sreangrathanak@yahoo.com"))
            startActivity(intent)
        }

        binding.btnFb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://web.facebook.com/khmerlang.official"))
            startActivity(intent)
        }

        binding.btnGithub.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/khmerlang"))
            startActivity(intent)
        }

        binding.btnWeb.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.khmerlang.com"))
            startActivity(intent)
        }

        binding.txtKhmerlang.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.khmerlang.com"))
            startActivity(intent)
        }
    }
}
