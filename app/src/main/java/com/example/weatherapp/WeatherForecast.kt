package com.example.weatherapp

import androidx.compose.runtime.Composable
import retrofit2.http.GET
import retrofit2.http.Query

@Composable
fun WeatherForecastScreen(){


}


data class WeatherForecast(
    val list : List<ForecastWeather>
)

data class ForecastWeather(
    val dt: Long,
    val main: Main,
    val weather: List<Weather>,
    val dt_txt: String
)

interface WeatherForecastAPI {
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherForecast
}