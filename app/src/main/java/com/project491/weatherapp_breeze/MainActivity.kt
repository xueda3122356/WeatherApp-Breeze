package com.project491.weatherapp_breeze

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.project491.weatherapp_breeze.adapter.RvAdapter
import com.project491.weatherapp_breeze.data.ForecastModels.ForecastData
import com.project491.weatherapp_breeze.databinding.ActivityMainBinding
import com.project491.weatherapp_breeze.utils.RetrofitInstance
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.HttpException
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

// Define the base URL for the API
private const val bURL = "https://api.weatherapi.com/v1/"

// Define the main activity class
class MainActivity : AppCompatActivity() {

    // Declare variable for view binding
    private lateinit var binding: ActivityMainBinding
    // Declare variables for location
    private lateinit var cityTextView: TextView
    private lateinit var stateTextView: TextView
    // Declare variables for current weather
    private lateinit var currentTempTextView: TextView
    private lateinit var maxTemp: TextView
    private lateinit var minTemp: TextView


    // Declared variable for Notification
    private val CHANNEL_ID = "channel_id_01"
    private val notificationID = 101


    // Define the activity creation function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)



        // Get the text view for displaying current temperature and weather
        currentTempTextView = binding.tvTemp

        // Get the text view for displaying min and max temperature
        maxTemp = binding.tvMaxTemp
        minTemp = binding.tvMinTemp

        // Get the status for switch button


        //create notification
        createNotification()

        // Set a click listener on the search button
        binding.searchButton.setOnClickListener {
            // Get the zip code entered by the user
            val location = binding.locationInput.text.toString()
            binding.locationInput.setText("")
            saveLocation(location)

            // Call the getMyData function with the entered zip code
            getCurrentData(location)
            getForecastData(location)
        }

        // Call the getMyData function with a default zip code
        //getMyData("92831")
        getCurrentData(loadLocation()!!)
        getForecastData(loadLocation()!!)

        // Get the city text views
        cityTextView = binding.headerLocationNameCity

        // Set the headerLocationName TextView to horizontally scroll if it's one word and too long
        //binding.headerLocationName.setHorizontallyScrolling(true)

        // set a click listener on the Forecast button
        binding.ForecastButton.setOnClickListener(){
            sendNotification()
        }
    }

    private fun getForecastData(location: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.apiForecast.getForecast(
                    location,"metric", applicationContext.getString(R.string.api_key)
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "http error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()!!

                    var forecastArray : ArrayList<ForecastData>

                    forecastArray = data.list as ArrayList<ForecastData>

                    val adapter = RvAdapter(forecastArray)
                    binding.rvForecast.adapter = adapter
                    binding.rvForecast.layoutManager = GridLayoutManager(
                        this@MainActivity,
                        1,
                        RecyclerView.HORIZONTAL,
                        false)

                }
            }
        }
    }


    // Define a function to retrieve weather data from the API
    private fun getCurrentData(location: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.apiCurrent.getCurrent(
                    location, "metric", applicationContext.getString(R.string.api_key)
                )
            } catch (e: IOException) {
                Toast.makeText(applicationContext, "app error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            } catch (e: HttpException) {
                Toast.makeText(applicationContext, "http error: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
                return@launch
            }
            if (response.isSuccessful && response.body() != null) {
                withContext(Dispatchers.Main) {
                    val data = response.body()!!

                    currentTempTextView.text = "${data.main.temp.toInt()} °C"

                    val iconId = data.weather[0].icon

                    val imgUrl = "https://api.openweathermap.org/img/w/$iconId.png"

                    Picasso.get().load(imgUrl).into(binding.imgWeather)

                    maxTemp.text = "Max temp: ${data.main.temp_max.toInt()} °C"
                    minTemp.text = "Min temp: ${data.main.temp_min.toInt()} °C"

                    binding.tvSunrise.text =
                        dateFormatConverter(
                            data.sys.sunrise.toLong()
                        )

                    binding.tvSunset.text =
                        dateFormatConverter(
                            data.sys.sunset.toLong()
                        )

                    binding.apply {
                        tvStatus.text = data.weather[0].description
                        tvWind.text = "${data.wind.speed.toString()} KM/H"
                        cityTextView.text = "${data.name}"
                        tvTemp.text = "${data.main.temp.toInt()} °C"
                        tvFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()} °C"
                        tvMinTemp.text = "Min temp: ${data.main.temp_min.toInt()} °C"
                        tvMaxTemp.text = "Max temp: ${data.main.temp_max.toInt()} °C"
                        tvHumidity.text = "${data.main.humidity}"
                        tvPressure.text = "${data.main.pressure}"
                        tvUpdateTime.text = "Last Update: ${
                            dateFormatConverter(
                                data.dt.toLong()
                            )
                        }"

                    }

                }
            }
        }
    }

    private fun dateFormatConverter(date: Long): String {
        return SimpleDateFormat(
            "hh:mm a",
            Locale.ENGLISH
        ).format(Date(date * 1000))

    }



    private fun createNotification(){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
        {
            val name = "Weather Breeze"
            val descriptionText = "Please don't forget to your umbrella"
            val importance = NotificationManager.IMPORTANCE_DEFAULT

            val channel = NotificationChannel(CHANNEL_ID,name,importance).apply {
                description = descriptionText
            }

            val notificationManager : NotificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)


        }
    }

    private fun sendNotification(){

        val intent: Intent = Intent(this, ForecastPage::class.java).apply {
            flags =Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }

        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, 0)

        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.sunny)
            .setContentTitle("Weather Breeze")
            .setContentText("Notification Example")
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)){
            if (ActivityCompat.checkSelfPermission(
                    applicationContext,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {

                return
            }
            notify(notificationID,builder.build())
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