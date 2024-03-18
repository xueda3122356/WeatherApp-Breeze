package com.project491.weatherapp_breeze

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Geocoder
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.Debug.getLocation
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.core.view.MenuItemCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.material.navigation.NavigationView
import com.project491.weatherapp_breeze.adapter.RvAdapter
import com.project491.weatherapp_breeze.adapter.RvAdapterUnitSwitch
import com.project491.weatherapp_breeze.data.ForecastModels.ForecastData
import com.project491.weatherapp_breeze.databinding.ActivityMainBinding
import com.project491.weatherapp_breeze.databinding.NavHeaderBinding
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



// Define the main activity class
class MainActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {

    // Declare variable for view binding
    private lateinit var binding: ActivityMainBinding
    // Declare variables for location
    private lateinit var cityTextView: TextView
    // Declare variables for current weather
    private lateinit var currentTempTextView: TextView
    private lateinit var maxTemp: TextView
    private lateinit var minTemp: TextView

    //
    private lateinit var UnitSwitch: Switch


    // Declared variable for Notification
    private val CHANNEL_ID = "channel_id_01"
    private val notificationID = 101


    // Declared variable for toolbar
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navView: NavigationView
    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var navController: NavController

    // Declared variable for Google's API for location services
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private val locationPermissionCode = 1000

    // Declared variable for location request
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback

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


        // Get navigation view
        drawerLayout = binding.drawerLayout
        navView = binding.navView


        // Using Google API
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        setupLocationClient()

        //create notification
        createNotification()

        // Set a click listener on the search button
        binding.searchButton.setOnClickListener {
            // Get the zip code entered by the user
            val location = binding.locationInput.text.toString()
            binding.locationInput.setText("")
            saveLocation(location)

            // Call the getMyData function with the entered zip code
            getCurrentDataCelsius(location)
            getForecastDataCelsius(location)

            // Reset Unit Switch Button
            UnitSwitch = findViewById(R.id.switch_for_nav_drawer)
            drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {
                override fun onDrawerOpened(drawerView: View) {
                    // Reset switch state here
                    UnitSwitch.isChecked = false // or true, depending on what you need
                }

                override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
                override fun onDrawerClosed(drawerView: View) {}
                override fun onDrawerStateChanged(newState: Int) {}
            })

        }

        // Call the getMyData function with a default zip code
        //getMyData("92831")
        getCurrentDataCelsius(loadLocation()!!)
        getForecastDataCelsius(loadLocation()!!)

        // Get the city text views
        cityTextView = binding.headerLocationNameCity

        // Set the headerLocationName TextView to horizontally scroll if it's one word and too long
        //binding.headerLocationName.setHorizontallyScrolling(true)

        // set a click listener on the Forecast button
        binding.ForecastButton.setOnClickListener(){
            sendNotification()
        }

        // setup toolbar and navigation drawer
        binding.apply {

            setSupportActionBar(toolbar)
            navView.bringToFront()
        }

        navController = findNavController(R.id.fragment)
        binding.navView.setupWithNavController(navController)

        appBarConfiguration = AppBarConfiguration(navController.graph, binding.drawerLayout)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.navView.setNavigationItemSelectedListener(this)

        // check unit switch
        val menuItem = navView.menu.findItem(R.id.nav_unit_switch)
        val actionView = menuItem.actionView as? Switch

        if (actionView == null) {
            val inflater =layoutInflater
            val switchLayout = inflater.inflate(R.layout.switch_item, navView, false)
            menuItem.actionView = switchLayout
            val switch = switchLayout.findViewById<Switch>(R.id.switch_for_nav_drawer)
            switch.setOnCheckedChangeListener{ _, isChecked ->
                // Handle switch check/change event here
                val location = cityTextView.text.toString()
                if(isChecked){
                    getCurrentDataFahrenheit(location)
                    getForecastDataFahrenheit(location)
                }
                else{
                    getCurrentDataCelsius(location)
                    getForecastDataCelsius(location)
                }
            }
        } else {
            actionView.setOnCheckedChangeListener { _, isChecked ->
                // Handle switch check/change event here
                val location = cityTextView.text.toString()
                Log.i("Switch", "${location}")
                if(isChecked){
                    getCurrentDataFahrenheit(location)
                    getForecastDataFahrenheit(location)
                }
                else{
                    getCurrentDataCelsius(location)
                    getForecastDataCelsius(location)
                }
            }
        }


    }




    private fun getForecastDataFahrenheit(location: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.apiForecast.getForecast(
                    location,"imperial", applicationContext.getString(R.string.api_key)
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

                    val adapter = RvAdapterUnitSwitch(forecastArray)
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

    private fun getCurrentDataFahrenheit(location: String) {
        GlobalScope.launch(Dispatchers.IO) {
            val response = try {
                RetrofitInstance.apiCurrent.getCurrent(
                    location, "imperial", applicationContext.getString(R.string.api_key)
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

                    currentTempTextView.text = "${data.main.temp.toInt()} °F"

                    val iconId = data.weather[0].icon

                    val imgUrl = "https://api.openweathermap.org/img/w/$iconId.png"

                    Picasso.get().load(imgUrl).into(binding.imgWeather)

                    maxTemp.text = "Max temp: ${data.main.temp_max.toInt()} °F"
                    minTemp.text = "Min temp: ${data.main.temp_min.toInt()} °F"

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
                        tvTemp.text = "${data.main.temp.toInt()} °F"
                        tvFeelsLike.text = "Feels like: ${data.main.feels_like.toInt()} °F"
                        tvMinTemp.text = "Min temp: ${data.main.temp_min.toInt()} °F"
                        tvMaxTemp.text = "Max temp: ${data.main.temp_max.toInt()} °F"
                        tvHumidity.text = "${data.main.humidity}"
                        tvPressure.text = "${data.main.pressure}"
                        tvUpdateTime.text = "Last Update: ${
                            dateFormatConverter(
                                data.dt.toLong()
                            )
                        }"


                    }
                    

                    modifyHeader(data.name)



                }
            }
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.fragment)
        return navController.navigateUp(appBarConfiguration)|| super.onSupportNavigateUp()
    }

    private fun getForecastDataCelsius(location: String) {
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
    private fun getCurrentDataCelsius(location: String) {
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

                    modifyHeader(data.name)



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

    private fun modifyHeader(location: String) {
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        val headerView: View = navigationView.getHeaderView(0)
        val userNameTextView: TextView = headerView.findViewById(R.id.nav_cityName)
        userNameTextView.text = "${location}"

    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        TODO("Not yet implemented")
    }

    private fun setupLocationClient() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                super.onLocationResult(locationResult)
                for(location in locationResult.locations){
                    // use the location data here
                    val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                    try{
                        // Use geocoder to get city name
                        val addresses = geocoder.getFromLocation(location.latitude, location.longitude, 1)
                        if(addresses != null)
                        {
                            val address = addresses[0]
                            val locationName =address.locality
                            Log.i("location", "${locationName} ${location.latitude} ${location.longitude}")

                            // save city name
                            saveLocation(locationName)


                        }
                    }catch (e: Exception){
                        e.printStackTrace()
                    }
                }
            }
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            // Permission is not granted, request it
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                locationPermissionCode)
        } else {
            // Permission has already been granted, start location updates
            startLocationUpdates()
        }
    }

    private fun startLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }


}