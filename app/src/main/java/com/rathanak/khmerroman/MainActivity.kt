package com.rathanak.khmerroman

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.rathanak.khmerroman.data.RomanItem
import io.realm.Realm
import java.io.IOException
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
            loadRoman2DB()
            prefs.edit().putBoolean("firstrun", false).commit()
        }

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
    data class RomanData(val r: String, val k: String) {}
    private fun loadRoman2DB() {

        val jsonFileString = getJsonDataFromAsset(applicationContext, "roman_khmer.json")
        val gson = Gson()
        val listPersonType = object : TypeToken<List<RomanData>>() {}.type

        var romans: List<RomanData> = gson.fromJson(jsonFileString, listPersonType)
        realm.beginTransaction()
        romans.forEach {
            var roman = it
            val romanKhmer: RomanItem = realm.createObject(RomanItem::class.java)
            romanKhmer.khmer = roman.k
            romanKhmer.roman = roman.r
            realm.insert(romanKhmer)
        }
        realm.commitTransaction()
    }
    private fun getJsonDataFromAsset(context: Context, fileName: String): String? {
        val jsonString: String
        try {
            jsonString = context.assets.open(fileName).bufferedReader().use { it.readText() }
        } catch (ioException: IOException) {
            ioException.printStackTrace()
            return null
        }
        return jsonString
    }
}
