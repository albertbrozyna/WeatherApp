package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalConsumer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.ui.platform.LocalContext
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    val context: Context = LocalContext.current
    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    var showFavorites = remember { mutableStateOf(false) }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        //Favourites list
        if (showFavorites.value) {
            Spacer(modifier = Modifier.padding(8.dp))

            Text("Favourite cities:")
            favoriteCities.value.forEach {
                Text(
                    " - $it\n",
                    modifier = Modifier.padding(start = 8.dp)
                )
            }

        }

        //Textfield to input cities
        TextField(
            value = city.value,
            onValueChange = { city.value = it },
            label = { Text("City") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.padding(16.dp))

        //Button to get weather
        Button(
            onClick = {
                coroutineScope.launch {

                    if (isNetworkConnectionAvailable(context)) {
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

                        weatherResponse.value?.let {
                            saveWeatherData(context, it)
                        }

                    } else {
                        weatherResponse.value = loadWeatherData(context)
                        Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show()
                    }


                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Get Weather")
        }

        //Button to add to favourites
        Button(
            onClick = {
                val cityName = city.value.trim()
                if (cityName.isNotEmpty()) {

                    if (favoriteCities.value.contains(cityName)) {
                        Toast.makeText(
                            context,
                            "$cityName is already in favourites.",
                            Toast.LENGTH_LONG
                        )
                            .show()
                        return@Button
                    }

                    favoriteCities.value = favoriteCities.value.toMutableList().apply {
                        add(cityName)
                    }
                    saveFavouriteCities(context, favoriteCities.value)
                    Toast.makeText(context, "$cityName added to favourites.", Toast.LENGTH_LONG)
                        .show()
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = Icons.Default.Star,
                contentDescription = "Add to favourites",
                tint = Color.Yellow
            )
        }

        //Button to show favourites
        Button(
            onClick = {
                showFavorites.value = !showFavorites.value
            }
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.List,
                contentDescription = "Show favorites"
            )
        }

        //Is loading text
        if (isLoading.value) {
            Text("Is Loading")
        } else {
            weatherResponse.value?.let {
                val iconCode = it.weather.firstOrNull()?.icon
                val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"


                Column(modifier = Modifier.padding(16.dp)) {
                    Text("City: ${it.name}")
                    Text("Temperature: ${it.main.temp}°C")
                    Text("Time: ${formatTime(it.dt)}")
                    Text("Description: ${it.weather.firstOrNull()?.description ?: ""}")
                    Text("Coordinates: ${it.coord.longitude}, ${it.coord.latitude}")
                    Text("Pressure: ${it.main.pressure}")

                    Spacer(modifier = Modifier.padding(8.dp))

                    AsyncImage(
                        model = iconUrl,
                        contentDescription = "Weather Icon",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        //Error message
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
    val coord: Coord,
    val weather: List<Weather>,
    val dt: Long
) : Serializable

data class Main(
    val temp: Float,
    val feelsLike: Float,
    val humidity: Float,
    val pressure: Int
) : Serializable

data class Coord(
    @SerializedName("lon") val longitude: Float,
    @SerializedName("lat") val latitude: Float
) : Serializable

data class Weather(
    val description: String,
    val icon: String
) : Serializable


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

fun formatTime(unixTime: Long): String {
    val date = Date(unixTime * 1000)
    val format = SimpleDateFormat("dd MMM yyyy HH:mm:ss", Locale.ENGLISH)
    return format.format(date)
}

fun isNetworkConnectionAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork

    if (network == null) {
        return false
    }

    val capabilities = connectivityManager.getNetworkCapabilities(network)

    if (capabilities == null) {
        return false
    }

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }

}

fun saveWeatherData(context: Context, weatherResponse: WeatherResponse) {
    try {
        val file = File(context.filesDir, "weatherData.txt")

        val fileOutputStream = FileOutputStream(file)

        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(weatherResponse)
        objectOutputStream.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadWeatherData(context: Context): WeatherResponse? {

    try {
        val file = File(context.filesDir, "weatherData.txt")

        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val weatherResponse = objectInputStream.readObject() as WeatherResponse
            objectInputStream.close()
            return weatherResponse

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun saveFavouriteCities(context: Context, cities: List<String>) {
    try {
        val file = File(context.filesDir, "favorite_cities.txt")
        file.writeText(cities.joinToString("\n"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadFavouriteCities(context: Context): List<String> {
    try {
        val file = File(context.filesDir, "favorite_cities.txt")
        if (file.exists()) {
            return file.readLines()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return emptyList()
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    WeatherAppTheme {
        WeatherScreen()
    }
}