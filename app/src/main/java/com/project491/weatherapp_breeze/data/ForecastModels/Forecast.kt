package com.project491.weatherapp_breeze.data.ForecastModels

data class Forecast(
    val city: City,
    val cnt: Int,
    val cod: String,
    val list: List<ForecastData>,
    val message: Int
)