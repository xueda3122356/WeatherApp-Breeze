package com.project491.weatherapp_breeze

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class StartPage : AppCompatActivity() {

    private lateinit var locationEditText: EditText
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        val startButton: Button = findViewById(R.id.startButton)

        locationEditText = findViewById(R.id.locationEditText)


        startButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            val location = locationEditText.text.toString()
            if(location != "")
            {
                saveLocation(location)
            }

            startActivity(intent)

            finish()
        }
    }

    private fun saveLocation(location: String) {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.apply {
            putString("STRING_KEY", location)
        }.apply()
    }

    private fun loadLocation(): String? {
        val sharedPreferences = getSharedPreferences("sharedPrefs", Context.MODE_PRIVATE)
        return sharedPreferences.getString("STRING_KEY", "92831")
    }
}
