package com.project491.weatherapp_breeze.data.models

data class weatherData(
    val alerts: Alerts,
    val current: Current,
    val forecast: Forecast,
    val location: Location
)