package com.project491.weatherapp_breeze

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class StartPage : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_start_page)

        val startButton: Button = findViewById(R.id.startButton)
        startButton.setOnClickListener {

            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)

            finish()
        }
    }
}
