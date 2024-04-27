package com.project491.weatherapp_breeze

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
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
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
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
import com.project491.weatherapp_breeze.utils.RetrofitInstance
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import android.widget.Button
import androidx.core.view.GravityCompat
import com.project491.weatherapp_breeze.databinding.ActivityForecastPageBinding
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import java.net.HttpURLConnection
import java.net.URL
import java.util.Calendar
import java.util.Timer
import kotlin.concurrent.timerTask
import kotlin.math.abs


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
    var currentTemperature = 0

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

    // Declared variable for forecast arrary
    private lateinit var celciusForecastArray: ArrayList<ForecastData>
    private lateinit var fahrenheitForecastArray: ArrayList<ForecastData>

    // Declared variable for forecast page
    var tempDecrease = false
    var tempIncrease = true
    var tempDifferent = 0
    var messageForecast = ""
    var highPercentage = false

    // Define the activity creation function
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // Inflate the layout using view binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //createNotificationChannel()
        //setupNotificationButton()




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
        //createNotification()

        // Set a click listener on the search button
        binding.searchButton.setOnClickListener {
            // Get the zip code entered by the user
            val location = binding.locationInput.text.toString()
            binding.locationInput.setText("")
            saveLocation(location)
            UnitSwitch = findViewById(R.id.switch_for_nav_drawer)

            // Call weather API and check unit switch button's state
            if(UnitSwitch.isChecked == true)
            {
                getCurrentDataFahrenheit(location)
                getForecastDataFahrenheit(location)
            }
            else
            {
                getCurrentDataCelsius(location)
                getForecastDataCelsius(location)
            }



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
        /*binding.ForecastButton.setOnClickListener(){
            val intent = Intent(this, ForecastPage::class.java)
            calTempDifferent()
            weatherLookAhead()
            intent.putExtra("tempIncrease", tempIncrease)
            intent.putExtra("tempDecrease", tempDecrease)
            intent.putExtra("tempDifferent", tempDifferent)
            intent.putExtra("message", messageForecast)
            startActivity(intent)
            messageForecast = ""
        }*/

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

        // check the frequency of pushing notification
        if(loadNotificationFrequency()!! == "one_hour_noti")
        {
            checkWeatherBaseOnFrequency(3600000,3600000)
        }
        else if(loadNotificationFrequency()!! == "three_hours_noti")
        {
            checkWeatherBaseOnFrequency(10800000,10800000)
        }
        else if(loadNotificationFrequency()!! == "one_day_noti")
        {
            checkWeatherBaseOnFrequency(86400000,86400000)
        }
        else if(loadNotificationFrequency()!! == "never_noti")
        {
            Log.i("Frequency", "never")
        }

        val date = Date()
        val cal = Calendar.getInstance()
        Log.i("calender", "${cal.get(Calendar.HOUR_OF_DAY)}")

    }

    private fun calTempDifferent() {
        val hoursAhead = 2

        // Check temperature
        var temperature = emptyArray<Int>()
        for(i in 0..hoursAhead) {
            temperature += celciusForecastArray[i].main.temp.toInt()

        }

        for(i in 0 .. hoursAhead-1)
        {
            tempDifferent += temperature[i] - temperature[i+1]
            tempIncrease = tempDifferent < 0
            tempDecrease = tempDifferent > 0
        }
        Log.i("tempdifferent", "${tempDifferent}")

    }


    private fun getPrefValueFromMenuId(id: Int): String? {
        return when(id){
            R.id.one_hour_noti -> "one_hour_noti"
            R.id.three_hours_noti -> "three_hours_noti"
            R.id.one_day_noti -> "one_day_noti"
            R.id.never_noti -> "never_noti"
            else -> "one_hour_noti"
        }
    }


    private fun getMenuIdFromPref(prefValue: String?): Int {
        return when(prefValue){
            "three_hours_noti" -> R.id.three_hours_noti
            "one_day_noti" -> R.id.one_day_noti
            "never_noti" -> R.id.never_noti
            "one_hour_noti" -> R.id.one_hour_noti
            else -> R.id.one_hour_noti
        }

    }

    /*private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            // Register the channel with the system
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }*/

    /*private fun setupNotificationButton() {
        val notifyButton = findViewById<Button>(R.id.notifyButton)
        notifyButton.setOnClickListener {
            createNotification()
        }
    }*/

    // Define a function to retrieve weather data from the API

    private fun refreshCurrentDataCelsius(location: String){
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
                    currentTemperature = data.main.temp.toInt()
                }

            }
        }
    }

    private fun refreshForecastDataCelsius(location: String){
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
                    Log.i("time","${data.list[0].dt_txt}")

                    //var forecastArray : ArrayList<ForecastData>

                    //forecastArray = data.list as ArrayList<ForecastData>
                    celciusForecastArray = data.list as ArrayList<ForecastData>

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

                    //var forecastArray : ArrayList<ForecastData>

                    //forecastArray = data.list as ArrayList<ForecastData>
                    fahrenheitForecastArray = data.list as ArrayList<ForecastData>

                    //val adapter = RvAdapterUnitSwitch(forecastArray)
                    val adapter = RvAdapterUnitSwitch(fahrenheitForecastArray)
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
                    Log.i("time","${data.list[0].dt_txt}")

                    //var forecastArray : ArrayList<ForecastData>

                    //forecastArray = data.list as ArrayList<ForecastData>
                    celciusForecastArray = data.list as ArrayList<ForecastData>

                    //val adapter = RvAdapter(forecastArray)
                    val adapter = RvAdapter(celciusForecastArray)
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
                    currentTemperature = data.main.temp.toInt()

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


    private fun dateFormatConverter(date: Long): String {
        return SimpleDateFormat(
            "hh:mm a",
            Locale.ENGLISH
        ).format(Date(date * 1000))

    }

   /* private fun createNotification(){
        val notificationId = 101 // or any other unique integer
        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.notification_icon)
            .setContentTitle("Sample Notification")
            .setContentText("This is an example")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        with(NotificationManagerCompat.from(this)) {
            if (ActivityCompat.checkSelfPermission(
                    this@MainActivity,
                    Manifest.permission.POST_NOTIFICATIONS
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
            notify(notificationId, builder.build())
        }
    }*/


    private fun sendNotification(){

        var message = weatherLookAhead()

        val intent: Intent = Intent(this, ForecastPage::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
            intent.putExtra("tempIncrease", tempIncrease)
            Log.i("tempIncrease", "${tempIncrease}")
            intent.putExtra("tempDecrease", tempDecrease)
            Log.i("tempDecrease", "${tempDecrease}")
            intent.putExtra("tempDifferent", tempDifferent)
            Log.i("tempDifferent", "$tempDifferent")
            intent.putExtra("message", messageForecast)
            Log.i("messageForecast", "$messageForecast")
            tempIncrease = false
            tempDecrease = false
            tempDifferent = 0


        val pendingIntent: PendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT)

        val imageIcon = celciusForecastArray[0].weather[0].icon
        val imageUrl = "https://api.openweathermap.org/img/w/$imageIcon.png"

        // Create a URL object
        val url = URL(imageUrl)
        // Open a connection
        val connection = url.openConnection() as HttpURLConnection
        connection.doInput = true
        connection.connect()
        // Get the InputStream
        val inputStream = connection.inputStream
        // Convert the InputStream into a Bitmap
        val bitmapLargeIcon = BitmapFactory.decodeStream(inputStream)
        // Close the InputStream
        inputStream.close()


        val builder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.noti_breege_icon)
            .setContentTitle("Weather Breeze")
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setLargeIcon(bitmapLargeIcon)
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


    private fun checkWeatherBaseOnFrequency(delay: Long, period: Long){
        val timer = Timer()

        timer.scheduleAtFixedRate(timerTask {
            sendNotification()
            }, delay, period)
            Log.i("delay", "${delay}")


    }

    private fun weatherLookAhead(): String {

        var message = ""
        messageForecast = ""

        refreshCurrentDataCelsius(loadLocation()!!)
        refreshForecastDataCelsius(loadLocation()!!)

        calTempDifferent()

        message += "${loadLocation()!!}: "
        message += "Temp: ${celciusForecastArray[0].main.temp.toInt()} °C "
        message += "Rain: ${(celciusForecastArray[0].pop * 100).toInt()} % "
        message += "Wind: ${celciusForecastArray[0].wind.speed} Km/H \n"
        message += "${celciusForecastArray[0].weather[0].description} \n\n"

        var hoursAhead = 2 // check in three hours

        var rainPercentage = emptyArray<Double>()
        for(i in 0 .. hoursAhead)
        {
            rainPercentage += celciusForecastArray[i].pop
        }

        var temperature = emptyArray<Double>()
        for(i in 0 .. hoursAhead)
        {
            temperature += celciusForecastArray[i].main.temp
        }

        var windSpeed = emptyArray<Double>()
        for(i in 0 .. hoursAhead)
        {
            windSpeed += celciusForecastArray[i].wind.speed
        }

        var description = emptyArray<String>()
        for(i in 0 .. hoursAhead)
        {
            description += celciusForecastArray[i].weather[0].description
        }

        // check situation and add suggestion in message

        for(i in 0 .. hoursAhead)
        {
            if(rainPercentage[i] >= 0.7)
            {
                highPercentage = true
                if(description[i] == "heavy intensity rain" || description[i] == "very heavy rain"
                    || description[i] == "extreme rain" || description[i] == "heavy intensity shower rain")
                {
                    messageForecast += "Heavy rain is expected in the next three hours. " +
                            "Please reduce going out, and if you need to go out," +
                            " please remember to bring an umbrella."
                    break
                }
                else if(description[i] == "light rain" || description[i] == "moderate rain"
                    || description[i] == "light intensity shower rain" || description[i] == "shower rain"
                    || description[i] == "ragged shower rain")
                {
                    messageForecast += "Light to moderate rain is expected in the next 3 hours. " +
                            "Please don't forget to bring an umbrella when you go out."
                    break
                }
                else if(description[i] == "freezing rain")
                {
                    messageForecast += "Freezing rain is expected in the next three hours." +
                            " If you need to go out, please remember to bring an umbrella" +
                            " and pay attention to slippery roads."
                    break
                }
            }
            else{

            }
        }


        if(tempIncrease)
        {

            if(abs(tempDifferent) >= 5 && currentTemperature >= 20){
                messageForecast += "As temperatures rise significantly, " +
                        "remember to bring a short-sleeve shirt " +
                        "and apply sunscreen if you go out."
            }
            else if(abs(tempDifferent) >= 3 && currentTemperature >= 20){
                messageForecast += "The temperature is rising slightly; please adjust your clothing accordingly."
            }
            else if(currentTemperature >= 20){
                messageForecast += "The weather is hot, please remember to stay hydrated."
            }

        }

        if(tempDecrease)
        {
            if(tempDifferent >= 5 && currentTemperature <= 15){
                messageForecast += "The temperature is noticeably dropping; remember to bring warm clothes if you go out."
            }
            else if(tempDifferent >= 3 && currentTemperature <= 15){
                messageForecast += "The temperature is slightly dropping; please ensure to keep warm appropriately."
            }
            else if(currentTemperature <= 15){
                messageForecast += "The weather is getting cooler, please remember to keep warm"
            }
        }

        if(!tempIncrease && !tempDecrease)
        {
            if(currentTemperature >= 20)
            {
                messageForecast += "The weather is hot, please remember to stay hydrated."
            }
            else if(currentTemperature <= 15)
            {
                messageForecast += "The weather is getting cooler, please remember to keep warm"
            }
            else
            {
                messageForecast += "good weather"
            }
        }


        message += messageForecast
        Log.i("messageForecast1", "${messageForecast}")
        return message
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
        when (item.itemId) {
            R.id.one_hour_noti,
            R.id.three_hours_noti,
            R.id.one_day_noti,
            R.id.never_noti -> {
                item.isChecked = true
                saveNotificationFrequency(item.itemId)
                if(item.itemId == R.id.one_hour_noti)
                {
                    checkWeatherBaseOnFrequency(10000,10000)
                }
                else if(item.itemId == R.id.three_hours_noti)
                {
                    checkWeatherBaseOnFrequency(10800000,10800000)
                }
                else if(item.itemId == R.id.one_day_noti)
                {
                    checkWeatherBaseOnFrequency(86400000,86400000)
                }
                else if(item.itemId == R.id.never_noti)
                {
                    Log.i("frequency", "never")
                }
                return true
            }
        }

        return false
    }

    private fun saveNotificationFrequency(selectedItemId: Int) {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("notificationFrequency", getPrefValueFromMenuId(selectedItemId))
            apply()
        }
    }

    private fun loadNotificationFrequency(): String? {
        val sharedPreferences = getSharedPreferences("MyAppPreferences", Context.MODE_PRIVATE)
        val notificationFrequency = sharedPreferences.getString("notificationFrequency", "never_noti")
        val menuId = getMenuIdFromPref(notificationFrequency)
        val navigationView: NavigationView = findViewById(R.id.nav_view)
        navigationView.setCheckedItem(menuId)
        return notificationFrequency
    }


    private fun setupLocationClient() {
        locationRequest = LocationRequest.create().apply {
            interval = 30000
            fastestInterval = 25000
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
                            Log.i("location", "${locationName}")


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

    /*override fun onPause() {
        super.onPause()
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }*/

    override fun onResume() {
        super.onResume()
        loadNotificationFrequency()

    }


}