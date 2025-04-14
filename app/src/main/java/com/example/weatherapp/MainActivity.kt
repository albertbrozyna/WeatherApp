package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize()
                ) {
                    WeatherScreen()
                }
            }
        }
    }
}


@Composable
fun WeatherScreen() {
    var city = remember { mutableStateOf("") }
    var weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        TextField(
            value = city.value,
            onValueChange = { city.value = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(16.dp))

        Button(
            onClick = {
                coroutineScope.launch {
                    isLoading.value = true
                    val result = fetchWeatherData(city.value)

                    if (result == null) {
                        weatherResponse.value = null
                        error.value = "City not found"
                    } else {
                        weatherResponse.value = result
                        error.value = null
                    }

                    isLoading.value = false
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Weather")
        }


        if (isLoading.value) {
            Text("Is Loading")
        } else {
            weatherResponse.value?.let {
                Text(
                    text = """
                City: ${it.name}
                Temperature: ${it.main.temp}°C
                Humidity: ${it.main.humidity}%
                Description: ${it.weather.firstOrNull()?.description ?: ""}
                Coordinates: ${it.coord.latitude}, ${it.coord.longitude}
                
            """.trimIndent(),
                    modifier = Modifier.padding(16.dp)
                )
            }

        }

        error.value?.let {
            Text(
                text = it,
                modifier = Modifier.padding(16.dp)
            )
        }

    }
}


data class WeatherResponse(
    val name: String,
    val main: Main,
    val coord: Coordinates,
    val weather: List<Weather>,
)

data class Main(
    val temp: Float,
    val feelsLike: Float,
    val humidity: Float
)

data class Coordinates(
    val longitude: Float,
    val latitude: Float
)

data class Weather(
    val description: String,
    val icon: String
)


interface WeatherAPI {
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherResponse
}

suspend fun fetchWeatherData(city: String): WeatherResponse? {
    try {
        val weatherAPI = WeatherApiClient.weatherAPI
        val response = weatherAPI.getWeatherByCity(city, "880c3b528650d9c1fbb39efcbd7e6fb3")

        return response
    } catch (e: Exception) {
        e.printStackTrace()

        if (e.message?.contains("HTTP 404") == true) {
            return null
        }

        return null
    }

}

fun isNetworkConnectionAvailable(): Boolean {
  return true
}


@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}