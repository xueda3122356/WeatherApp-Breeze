package com.project491.weatherapp_breeze.data

import com.project491.weatherapp_breeze.data.models.weatherData
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

private const val key = "e55db302eb8c403a89801251232504"

interface ApiInterface {
    //Query takes a parameter which should match links query, and takes in a variable
    //Query output -> "&parameter=variable" into URL, query placement in URL doesn't matter
    @GET("forecast.json?key=e55db302eb8c403a89801251232504&days=3&aqi=yes&alerts=yes")
    fun getForecastData(@Query("q") location:String): Call<weatherData>

}