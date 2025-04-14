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
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.Modifier
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.HorizontalAlignmentLine
import androidx.compose.ui.modifier.ModifierLocalConsumer

import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.google.gson.annotations.SerializedName
import org.intellij.lang.annotations.JdkConstants
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.format.TextStyle
import java.util.Date
import java.util.Locale

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme(darkTheme = true) {
                var selectedScreen = remember { mutableIntStateOf(0) }

                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    bottomBar = { BottomNavigationBar(selectedScreen) }
                ) {
                    when (selectedScreen.value) {
                        0 -> WeatherScreen()
                        1 -> WeatherForecastScreen()
                        2 -> SettingsScreen()
                    }
                }
            }
        }
    }
}

@Composable
fun BottomNavigationBar(selectedScreen : MutableState<Int>) {

    NavigationBar(
        modifier = Modifier.padding(16.dp),
    ) {
        NavigationBarItem(
            onClick = {selectedScreen.value = 0},
            selected = selectedScreen.value == 0,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home,
                    contentDescription = "Home"
                )
            },
            label = {Text("Home")}

        )
        NavigationBarItem(
            onClick = {selectedScreen.value = 1},
            icon = {
                Icon(
                    imageVector = Icons.Default.FavoriteBorder,
                    contentDescription = "Weather forecast",

                )
            },
            selected = selectedScreen.value == 1,
            label = {Text("Weather forecast")}
        )


        NavigationBarItem(
            onClick = {selectedScreen.value = 2},
            icon = {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings"
                )

            },
            selected = selectedScreen.value == 2,
            label = {Text("Settings")}
        )
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

        CitiesSection(favoriteCities, showFavorites,city,context)

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
                            saveWeatherData(context, it, "weatherData.txt")
                        }

                    } else {
                        weatherResponse.value = loadWeatherData(context, "weatherData.txt")
                        Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show()
                    }


                }
            },
            modifier = Modifier.fillMaxWidth().padding(start = 6.dp,top = 8.dp,end = 6.dp),
            shape = RoundedCornerShape(6.dp)
        ) {
            Text("Get Weather")
        }

        //Is loading text
        if (isLoading.value) {
            Text("Is Loading")
        } else {
            weatherResponse.value?.let {
                WeatherInfo(it)
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


@Composable
fun WeatherInfo(weatherResponse: WeatherResponse){
    val iconCode = weatherResponse.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"


    Column(modifier = Modifier.padding(16.dp).fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            weatherResponse.name,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AsyncImage(
            model = iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier.size(24.dp)
        )


        Text("${weatherResponse.main.temp}°C",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)


        Text("Description: ${weatherResponse.weather.firstOrNull()?.description ?: ""}",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center)


        Text("Time: ${formatTime(weatherResponse.dt)}")

        Text("Coordinates: ${weatherResponse.coord.longitude}, ${weatherResponse.coord.latitude}")
        Text("Pressure: ${weatherResponse.main.pressure}")
        Text("Humidity: ${weatherResponse.main.pressure}")
        Text("Wind Speed: ${weatherResponse.wind.speed} m/s")
        Text("Wind Direction: ${weatherResponse.wind.deg}°")

        Spacer(modifier = Modifier.padding(8.dp))


    }
}



@Composable
fun CitiesSection(favoriteCities : MutableState<List<String>>,
                  showFavorites : MutableState<Boolean>,
                  city : MutableState<String>,
                  context :Context ) {
    Column(
        modifier = Modifier.fillMaxWidth(),

    ){
        //Favorities list
        if (showFavorites.value) {


            Text("Favourite cities:",
                modifier = Modifier.padding(8.dp))

            favoriteCities.value.forEach { cityName ->

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    Text(
                        " - $cityName\n",
                        modifier = Modifier.padding(start = 8.dp),
                        textAlign = TextAlign.Center,
                        maxLines = 1
                    )
                    IconButton(
                        onClick = {
                            favoriteCities.value = favoriteCities.value.toMutableList().apply {
                                remove(cityName)
                                saveFavouriteCities(context, favoriteCities.value)
                            }
                        },
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Remove from favorites",
                            modifier = Modifier.padding(start = 8.dp),
                            tint = Color.Red
                        )
                    }

                }

            }
        }

        Row(modifier = Modifier
            .fillMaxWidth().height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ){
            //Textfield to input cities
            TextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("City") },
                modifier = Modifier.weight(8f).padding(top = 6.dp),
                shape = RoundedCornerShape(6.dp)
            )

            //Button for adding to favorities
            IconButton(
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
                            return@IconButton
                        }

                        favoriteCities.value = favoriteCities.value.toMutableList().apply {
                            add(cityName)
                        }
                        saveFavouriteCities(context, favoriteCities.value)
                        Toast.makeText(context, "$cityName added to favourites.", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                modifier = Modifier.weight(1f).padding(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Add to favourites",
                    tint = Color.Yellow,
                )
            }

            IconButton(
                onClick = { showFavorites.value = !showFavorites.value },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_list_24),
                    contentDescription = "Show favorites"
                )
            }

        }
    }

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

data class Wind(
    val speed: Float,
    val deg: Int
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
    val format = SimpleDateFormat("HH:mm:ss\ndd MMM yyyy", Locale.ENGLISH)
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

fun saveWeatherData(context: Context, weatherResponse: WeatherResponse,filename : String) {
    try {
        val file = File(context.filesDir, filename)

        val fileOutputStream = FileOutputStream(file)

        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(weatherResponse)
        objectOutputStream.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadWeatherData(context: Context, filename: String): WeatherResponse? {

    try {
        val file = File(context.filesDir,filename)

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