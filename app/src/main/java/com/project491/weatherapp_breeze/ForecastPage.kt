package com.project491.weatherapp_breeze

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import java.lang.Math.abs

class ForecastPage : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forecast_page)

        val btBack = findViewById<Button>(R.id.bt_back)

        suggestionBar()

        btBack.setOnClickListener(){
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }

    private fun suggestionBar() {

        val tempIncrease = intent.getBooleanExtra("tempIncrease", false)
        Log.i("tempIncrease", "$tempIncrease")
        val tempDecrease = intent.getBooleanExtra("tempDecrease", false)
        Log.i("tempDecrease", "${tempDecrease}")
        val tempDifferent = intent.getIntExtra("tempDifferent", 0)
        Log.i("tempDifferent", "$tempDifferent")
        val message = intent.getStringExtra("message")
        Log.i("message2", "$message")


        if(message != null)
        {
            var tvMessage = findViewById<TextView>(R.id.tvSuggestion)
            tvMessage.setText(message)
        }


        var tvTempChange = findViewById<TextView>(R.id.tvTempChange)
        var tvTempDescription = findViewById<TextView>(R.id.tvTempDescription)

        if(tempIncrease)
        {
            tvTempChange.setText("Temperature: ${abs(tempDifferent)} °C increase")
            if(tempDifferent >= 6){
                tvTempDescription.setText("Description:  Highly Temperature Increases")
            }
            else if(tempDifferent >= 3){
                tvTempDescription.setText("Description:  Slightly Temperature Increases")
            }
            else{
                tvTempDescription.setText("Description:  No significant changes in temperature")
            }

        }
        if(tempDecrease)
        {
            tvTempChange.setText("Temperature: ${abs(tempDifferent)} °C decrease")
            if(tempDifferent >= 6){
                tvTempDescription.setText("Description:  Highly Temperature Decreases")
            }
            else if(tempDifferent >= 3){
                tvTempDescription.setText("Description:  Slightly Temperature Decreases")
            }
            else{
                tvTempDescription.setText("Description:  No significant changes in temperature")
            }
        }

        if(tempDifferent == 0)
        {
            tvTempChange.setText("Temperature: ${abs(tempDifferent)} °C increase")
            tvTempDescription.setText("Description:  No significant changes in temperature")
        }


    }
}
