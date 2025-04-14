package com.example.weatherapp

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

@Composable
fun WeatherForecastScreen(){
    var city = remember { mutableStateOf("") }
    var weatherForecast = remember { mutableStateOf<WeatherForecast?>(null) }
    val context: Context = LocalContext.current
    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    var showFavorites = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    CitiesSection(favoriteCities, showFavorites,city,context)

    /*Button(


        onClick = {
            scope.launch {  weatherForecast = WeatherApiClient.weatherAPI.}
            }
    ){

    }*/

}


@Composable
fun WeekDaysForecast(){

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