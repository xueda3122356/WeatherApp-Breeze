package com.project491.weatherapp_breeze

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

private const val key = "627b37bf9c7949a2a8042815231104"

interface ApiInterface {
    //Query takes a parameter which should match links query, and takes in a variable
    //Query output -> "&parameter=variable" into URL, query placement in URL doesn't matter
    @GET("forecast.json?key=627b37bf9c7949a2a8042815231104&days=3&aqi=yes")
    fun getForecastData(@Query("q") location:String): Call<weatherData>

}