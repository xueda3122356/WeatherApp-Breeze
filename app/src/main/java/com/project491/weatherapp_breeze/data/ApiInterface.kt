package com.project491.weatherapp_breeze.data

import com.project491.weatherapp_breeze.data.ForecastModels.Forecast
import com.project491.weatherapp_breeze.data.models.Current
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query


interface ApiInterface {
    //Query takes a parameter which should match links query, and takes in a variable
    //Query output -> "&parameter=variable" into URL, query placement in URL doesn't matter
    @GET("weather?")
    suspend fun getCurrent(
        @Query("q") city: String,
        @Query("units") units: String,
        @Query("appid") api_key: String,
    ): Response<Current>

    @GET("hourly?")
    suspend fun getForecast(
        @Query("q") city :String,
        @Query("units") units: String,
        @Query("appid") apiKey: String,
    ):Response<Forecast>

}