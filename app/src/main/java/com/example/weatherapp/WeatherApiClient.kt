package com.example.weatherapp

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object WeatherApiClient{

    private const val URL = "https://api.openweathermap.org/"

    val retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherAPI : WeatherAPI = retrofit.create(WeatherAPI::class.java)
}