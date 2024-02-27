package com.project491.weatherapp_breeze.utils

import com.project491.weatherapp_breeze.data.ApiInterface
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitInstance {
    val apiCurrent: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }

    val apiForecast: ApiInterface by lazy {
        Retrofit.Builder()
            .baseUrl(Util.Base1)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiInterface::class.java)
    }
}