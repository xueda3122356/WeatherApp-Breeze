package com.project491.weatherapp_breeze.data.models

class ConvertData (day: Forecastday) {
    val dataList = arrayListOf<String>()

    init {
        dataList.add(day.date)
        dataList.add(day.day.avgtemp_c.toString())
        dataList.add(day.day.avgtemp_f.toString())
        dataList.add(day.day.maxtemp_c.toString())
        dataList.add(day.day.maxtemp_f.toString())
        dataList.add(day.day.mintemp_c.toString())
        dataList.add(day.day.mintemp_f.toString())
        dataList.add(day.day.condition.text)
        dataList.add(day.day.avghumidity.toString())
        dataList.add(day.day.uv.toString())
    }

    fun get(): ArrayList<String> {
        return dataList
    }
}