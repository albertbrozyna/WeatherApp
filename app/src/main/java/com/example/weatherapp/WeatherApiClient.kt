package com.example.weatherapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.Serializable

object WeatherApiClient{

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor)
        .build()

    private const val URL = "https://api.openweathermap.org"

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val weatherAPI : WeatherAPI = retrofit.create(WeatherAPI::class.java)
    val weatherForecastAPI : WeatherForecastAPI = retrofit.create(WeatherForecastAPI::class.java)
}

//Getting forecast for next days
suspend fun fetchWeatherForecast(city: String, apiKey: String): WeatherForecastList? {
    return try {
        val weatherForecastAPI = WeatherApiClient.weatherForecastAPI
        weatherForecastAPI.getWeatherForecastByCity(city, apiKey)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//Data structures

data class WeatherForecastList(
    val list: List<ForecastWeather>
) : java.io.Serializable

data class ForecastWeather(
    val dt: Long, val main: Main, val weather: List<Weather>, val dt_txt: String
) : Serializable

interface WeatherForecastAPI {
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherForecastList
}

data class WeatherResponse(
    val name: String,
    val main: Main,
    val coord: Coord,
    val visibility: Int,
    val wind: Wind,
    val weather: List<Weather>,
    val dt: Long
) : Serializable

data class Main(
    val temp: Float, val feelsLike: Float, val humidity: Float, val pressure: Int
) : Serializable

data class Coord(
    val lon: Float,
    val lat: Float
) : Serializable

data class Weather(
    val description: String, val icon: String
) : Serializable

data class Wind(
    val speed: Float, val deg: Int
) : Serializable


interface WeatherAPI {
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}