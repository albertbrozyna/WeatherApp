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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
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
    var temperature = remember { mutableStateOf<String?>(null) }

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
                temperature.value = "22°C"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Weather")
        }

        temperature.value?.let {
            Text(
                text = "Current temperature in ${city.value} is $it",
                modifier = Modifier.padding(16.dp)
            )

        }

    }
}


data class WeatherResponse(
    val name :String,
    val main : Main
)

data class Main(
    val temp: Float
)

interface WeatherAPI {
    @GET("data/2.5/weather")
    suspend fun getWeatherByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ) : WeatherResponse
}

fun fetchWeatherData(){
    val weatherAPI = WeatherApiClient.weatherAPI
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {

}