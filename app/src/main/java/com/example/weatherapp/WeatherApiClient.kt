package com.example.weatherapp

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.Serializable

object WeatherApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder().addInterceptor(loggingInterceptor).build()

    private const val URL = "https://api.openweathermap.org"

    val retrofit: Retrofit =
        Retrofit.Builder().baseUrl(URL).addConverterFactory(GsonConverterFactory.create()).build()

    // Api for weather
    val weatherAPI: WeatherAPI = retrofit.create(WeatherAPI::class.java)

    // Geocoding API
    val geocodingAPI: GeocodingAPI = retrofit.create(GeocodingAPI::class.java)

    val weatherForecastAPI: WeatherForecastAPI = retrofit.create(WeatherForecastAPI::class.java)
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
    val list: List<ForecastWeather>, val city: City
) : Serializable


data class City(
    val name: String
) : Serializable

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


// Classes for fetching multiple cities
data class GeoCity(
    val name: String,
    val lat: Double,
    val lon: Double,
    val country: String,
    val state: String? = null
) : Serializable

// For searching cities max 10
interface GeocodingAPI {
    @GET("/geo/1.0/direct")
    suspend fun getCityCoordinates(
        @Query("q") cityName: String,
        @Query("limit") limit: Int = 10,
        @Query("appid") apiKey: String
    ): List<GeoCity>
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
    val lon: Float, val lat: Float
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

    // Finding by coordinates
    @GET("data/2.5/weather")
    suspend fun getWeatherByCoordinates(
        @Query("lat") lat: Double,
        @Query("lon") lon: Double,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}