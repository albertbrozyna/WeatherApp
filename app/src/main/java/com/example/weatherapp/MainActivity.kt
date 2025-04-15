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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
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
import androidx.compose.material3.TextButton
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
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
import androidx.compose.ui.text.TextStyle as TxtStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.ui.theme.BottomNavigationBar
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.Serializable
import java.text.SimpleDateFormat
import java.time.LocalTime
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
                ) { innerPadding ->
                    when (selectedScreen.intValue) {
                        0 -> WeatherScreen(modifier = Modifier.padding(innerPadding))
                        1 -> WeatherForecastScreen(modifier = Modifier.padding(innerPadding))
                        2 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                    }
                }
            }
        }
    }
}

@Composable
fun WeatherScreen(modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    //Filename where weather is wrote
    val filenameWeather  = context.getString(R.string.last_city_weather_filename)
    //Key for last_weather in preferences
    val lastWeatherCityKey = context.getString(R.string.last_city_weather_key)
    var city = remember { mutableStateOf(loadPreference(context,lastWeatherCityKey) ?: "")  }
    var weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }
    val coroutineScope = rememberCoroutineScope()
    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    var favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    var showFavorites = remember { mutableStateOf(false) }
    val apiKey = context.getString(R.string.api_key)
    //Selecting background depending on hour
    val currentHour = remember { LocalTime.now().hour }

    val backgroundImage = when(currentHour){
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
    }

    LaunchedEffect(Unit) {
        //Loading last city
        if (city.value.isNotEmpty()) {
            weatherResponse.value = loadWeatherData(context, filenameWeather)
        }

        //Checking internet connection on start
        if (!isNetworkConnectionAvailable(context)){
            Toast.makeText(context, "No internet connection, displayed data might not be up to date.", Toast.LENGTH_LONG).show()
        }
    }

    Box(modifier = Modifier.fillMaxSize()){

        //Bcg image
        Image(
            painter = painterResource(id = backgroundImage),
            contentDescription = "Background",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        val scroll = rememberScrollState()

        Column(
            modifier = modifier
                .fillMaxWidth()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {

            CitiesSection(favoriteCities, showFavorites, city, context)

            //Button to get weather
            Button(
                onClick = {
                    coroutineScope.launch {

                        if (isNetworkConnectionAvailable(context)) {
                            isLoading.value = true
                            val result = fetchWeatherData(city.value,apiKey)

                            if (result == null) {
                                weatherResponse.value = null
                                error.value = "City not found"
                            } else {
                                weatherResponse.value = result
                                error.value = null
                            }

                            isLoading.value = false
                            //Saving prefernces
                            savePreference(context ,lastWeatherCityKey,city.value)

                            //Saving weather
                            weatherResponse.value?.let {
                                saveWeatherData(context, it, filenameWeather)
                            }

                        } else {
                            weatherResponse.value = loadWeatherData(context, filenameWeather)
                            Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG).show()
                        }


                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, top = 8.dp, end = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Get Weather", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            //Is loading text
            if (isLoading.value) {
                Text(
                    "Loading...",
                    textAlign = TextAlign.Center,
                    fontSize = 30.sp,
                    modifier = Modifier.padding(top = 20.dp)
                )
            } else {
                weatherResponse.value?.let {
                    WeatherInfo(it)
                }
            }

            //Error message
            error.value?.let {
                Toast.makeText(context, it, Toast.LENGTH_LONG).show()
            }

        }
    }
}


@Composable
fun WeatherInfo(weatherResponse: WeatherResponse) {
    //variables to icon
    val iconCode = weatherResponse.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"


    Column(
        modifier = Modifier
            .padding(16.dp)
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            weatherResponse.name,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AsyncImage(
            model = iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 8.dp)
        )


        Text(
            "${weatherResponse.main.temp.toInt()}°C",
            fontSize = 38.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        Text(
            weatherResponse.weather.firstOrNull()?.description ?: "",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp)
        )


        Text(
            text = formatTime(weatherResponse.dt),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        Row(horizontalArrangement = Arrangement.SpaceEvenly) {
            Text(
                "Pressure\n${weatherResponse.main.pressure}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(Modifier.width(16.dp))

            Text(
                "Humidity\n${weatherResponse.main.humidity}",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }


        Text(
            "Coordinates\n${weatherResponse.coord.lon}, ${weatherResponse.coord.lat}",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )


        HorizontalDivider(
            thickness = 0.7.dp, color = Color.White, modifier = Modifier.padding(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {
            Text(
                "Wind Speed\n ${weatherResponse.wind.speed} m/s",

                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(Modifier.width(16.dp))

            Text(
                "Wind Direction\n ${weatherResponse.wind.deg}°",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )
        }
    }
}


@Composable
fun CitiesSection(
    favoriteCities: MutableState<List<String>>,
    showFavorites: MutableState<Boolean>,
    city: MutableState<String>,
    context: Context
) {
    Column(
        modifier = Modifier.fillMaxWidth(),

        ) {
        //Favorities list
        if (showFavorites.value) {


            Text(
                "Favourite cities:", modifier = Modifier.padding(8.dp)
            )

            favoriteCities.value.forEach { cityName ->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        //Changing input value to cityName
                        onClick = {city.value = cityName}
                    ){
                        Text(" - $cityName\n",
                            modifier = Modifier.padding(start = 8.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1)
                    }
                    //Icon button do delete from favorite list
                    IconButton(
                        onClick = {
                            val updatedList = favoriteCities.value.toMutableList().apply {
                                remove(cityName)
                            }

                            favoriteCities.value = updatedList
                            saveFavouriteCities(context, updatedList)
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

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {



            //Textfield to input cities
            TextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("City", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TxtStyle(fontSize = 20.sp),
                modifier = Modifier
                    .weight(7f)
                    .padding(top = 6.dp),
                shape = RoundedCornerShape(6.dp),
                singleLine = true
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
                                Toast.LENGTH_LONG,
                            ).show()
                            return@IconButton
                        }

                        //Saving favorite cities
                        val updatedList = favoriteCities.value.toMutableList().apply {
                            add(cityName)
                        }
                        favoriteCities.value = updatedList

                        saveFavouriteCities(context,favoriteCities.value)

                        Toast.makeText(context, "$cityName added to favourites.", Toast.LENGTH_LONG)
                            .show()
                    }
                },
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp),
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = "Add to favourites",
                    tint = Color.Yellow,
                )
            }


            //Icon to show a list
            IconButton(
                onClick = {
                    //Loading favorite cities
                    favoriteCities.value = loadFavouriteCities(context)
                    showFavorites.value = !showFavorites.value },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_list_24),
                    contentDescription = "Show favorites"
                )
            }

            //Refresh icon

            IconButton(
                onClick = {},
                modifier = Modifier
                    .weight(1f)
                    .padding(0.dp)
            ){
                Icon(
                    painter = painterResource(R.drawable.baseline_refresh_24),
                    contentDescription = "Show favorites"
                )
            }

        }
    }

}




suspend fun fetchWeatherData(city: String,apiKey: String): WeatherResponse? {
    try {
        val weatherAPI = WeatherApiClient.weatherAPI
        val response = weatherAPI.getWeatherByCity(city, apiKey)

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
    val format = SimpleDateFormat("HH:mm\ndd MMM yyyy", Locale.ENGLISH)
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

fun saveWeatherData(context: Context, weatherResponse: WeatherResponse, filename: String) {
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
        val file = File(context.filesDir, filename)

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
        val filename = context.getString(R.string.favorite_cities)
        val file = File(context.filesDir, filename)
        file.writeText(cities.joinToString("\n"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadFavouriteCities(context: Context): List<String> {
    try {
        val filename = context.getString(R.string.favorite_cities)

        val file = File(context.filesDir, filename)
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
        WeatherForecastScreen()
    }
}