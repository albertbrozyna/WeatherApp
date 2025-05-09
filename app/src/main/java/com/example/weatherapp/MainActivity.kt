package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import androidx.compose.ui.text.TextStyle as TxtStyle

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme(darkTheme = true) {
                val context = LocalContext.current
                val selectedScreen = remember { mutableIntStateOf(0) }

                val tablet = remember { isTablet(context) }

                if (tablet) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBarTablet(selectedScreen) }) { innerPadding ->
                        when (selectedScreen.intValue) {
                            0 -> WeatherScreen(modifier = Modifier.padding(innerPadding),tablet = true)
                            1 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBar(selectedScreen) }) { innerPadding ->
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
}


@Composable
fun WeatherScreen(modifier: Modifier = Modifier, tablet: Boolean = false) {
    val context: Context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val lastWeatherCityKey = context.getString(R.string.last_city_weather_key)
    val city = remember { mutableStateOf(loadPreference(context, lastWeatherCityKey) ?: "") }

    val weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }
    //List of weather for favorite cities
    val weatherList = remember { mutableStateOf<List<WeatherResponse>>(emptyList()) }

    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }

    val isLoading = remember { mutableStateOf(false) }
    val showFavorites = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    //List of favorite cities

    //Keys
    val refreshTimeKey = context.getString(R.string.refresh_time_key)
    val refreshIntervalMinutes = loadPreference(context, refreshTimeKey)?.toIntOrNull() ?: 60L

    //For forecast for tablet

    val weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
    val weatherForecastList = remember { mutableStateOf<List<WeatherForecastList>>(emptyList()) }

    val currentCityShowed = remember { mutableStateOf("") }

    var unsued = remember { mutableStateOf<Boolean>(false) }
    //Delay
    LaunchedEffect(city.value) {
        val delayTime = (refreshIntervalMinutes.toLong() * 60L * 1000L)

        while (true) {
            delay(delayTime)
            if (city.value.isNotEmpty()) {
                updateWeather(
                    context, isLoading, city, weatherResponse, error, favoriteCities, weatherList
                )

                if (tablet) {
                    updateWeatherForecast(
                        context,
                        unsued,
                        city,
                        weatherForecast,
                        error,
                        favoriteCities,
                        weatherForecastList,
                        currentCityShowed
                    )
                }

            }
        }
    }

    //reload functionality to tell if wee need to update UI
    val reload = remember { mutableStateOf(false) }

    LaunchedEffect(reload.value) {
        updateWeather(context, isLoading, city, weatherResponse, error, favoriteCities, weatherList)

        if (tablet) {
            updateWeatherForecast(
                context,
                unsued,
                city,
                weatherForecast,
                error,
                favoriteCities,
                weatherForecastList,
                currentCityShowed
            )
        }

    }

    //Selecting background depending on hour
    val currentHour = remember { LocalTime.now().hour }

    val backgroundImage = when (currentHour) {
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
    }

    //Loading on start
    LaunchedEffect(Unit) {
        //Loading last city
        if (city.value.isNotEmpty()) {
            updateWeather(
                context, isLoading, city, weatherResponse, error, favoriteCities, weatherList
            )

            if (tablet) {
                updateWeatherForecast(
                    context,
                    unsued,
                    city,
                    weatherForecast,
                    error,
                    favoriteCities,
                    weatherForecastList,
                    currentCityShowed
                )
            }

        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                .fillMaxSize()
                .verticalScroll(scroll)
                .padding(16.dp)
        ) {

            CitiesSection(favoriteCities, showFavorites, city, context, reload)

            //Button to get weather
            Button(
                onClick = {
                    coroutineScope.launch {
                        updateWeather(
                            context,
                            isLoading,
                            city,
                            weatherResponse,
                            error,
                            favoriteCities,
                            weatherList
                        )

                        if(tablet){
                            updateWeatherForecast(
                                context,
                                unsued,
                                city,
                                weatherForecast,
                                error,
                                favoriteCities,
                                weatherForecastList,
                                currentCityShowed
                            )
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
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...", textAlign = TextAlign.Center, fontSize = 30.sp
                    )
                }

            } else {

                Row(
                    Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    Box(modifier = Modifier.weight(1f)) {
                        weatherResponse.value?.let {
                            WeatherInfo(context, it)
                        }
                    }

                    if (tablet) {
                        Box(modifier = Modifier.weight(1f)) {
                            weatherForecast.value?.let {
                                WeekDaysForecast(context, "", it)
                            }
                        }
                    }
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
fun CitiesSection(
    favoriteCities: MutableState<List<String>>,
    showFavorites: MutableState<Boolean>,
    city: MutableState<String>,
    context: Context,
    reload: MutableState<Boolean>,
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
                        onClick = { city.value = cityName }) {
                        Text(
                            " - $cityName\n",
                            modifier = Modifier.padding(start = 8.dp),
                            textAlign = TextAlign.Center,
                            maxLines = 1,
                            fontSize = 18.sp
                        )
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

                        saveFavouriteCities(context, favoriteCities.value)

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
                    showFavorites.value = !showFavorites.value
                },
                modifier = Modifier.weight(1f),
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_list_24),
                    contentDescription = "Show favorites"
                )
            }

            //Refresh icon
            IconButton(
                onClick = {
                    reload.value = !reload.value
                }, modifier = Modifier
                    .weight(1f)
                    .padding(0.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.baseline_refresh_24),
                    contentDescription = "Show favorites"
                )
            }

        }
    }

}


@Composable
fun WeatherInfo(context: Context, weatherResponse: WeatherResponse) {
    //variables to icon
    val iconCode = weatherResponse.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

    //keys
    val windUnitsKey = context.getString(R.string.wind_units_key)
    val tempUnitsKey = context.getString(R.string.temp_units_key)

    //units preferences
    val windUnits = loadPreference(context, windUnitsKey) ?: "m/s"
    val tempUnits = loadPreference(context, tempUnitsKey) ?: "metric"

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

        Text(
            "${weatherResponse.coord.lon}, ${weatherResponse.coord.lat}",
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        AsyncImage(
            model = iconUrl,
            contentDescription = "Weather Icon",
            modifier = Modifier
                .size(120.dp)
                .padding(bottom = 8.dp)
        )

        //For celc
        if (tempUnits == "metric") {
            Text(
                "${weatherResponse.main.temp.toInt()}°C",
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        } else {

            val tempF = convertTemperatureToF(weatherResponse.main.temp)
            Text(
                tempF,
                fontSize = 38.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        //Description
        Text(
            weatherResponse.weather.firstOrNull()?.description ?: "",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 10.dp)
        )

        //Time
        Text(
            text = formatTime(weatherResponse.dt),
            textAlign = TextAlign.Center,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
            modifier = Modifier.padding(bottom = 6.dp)
        )

        HorizontalDivider(
            thickness = 0.7.dp, color = Color.White, modifier = Modifier.padding(10.dp)
        )

        Row {
            Text(
                "Pressure\n${weatherResponse.main.pressure} hPa",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(Modifier.width(24.dp))

            Text(
                "Humidity\n${weatherResponse.main.humidity} %",
                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        HorizontalDivider(
            thickness = 0.7.dp, color = Color.White, modifier = Modifier.padding(10.dp)
        )

        Row(
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
        ) {

            //Wind speed
            val wind = if (windUnits == "mph") {
                convertWindSpeedToMph(weatherResponse.wind.speed)
            } else {
                "${weatherResponse.wind.speed} m/s"
            }
            Text(
                "Wind Speed\n $wind",

                textAlign = TextAlign.Center,
                fontWeight = FontWeight.Bold,
                fontSize = 24.sp,
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Spacer(Modifier.width(24.dp))

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


suspend fun updateWeather(
    context: Context,
    isLoading: MutableState<Boolean>,
    city: MutableState<String>,
    weatherResponse: MutableState<WeatherResponse?>,
    error: MutableState<String?>,
    favoriteCities: MutableState<List<String>>,
    weatherList: MutableState<List<WeatherResponse>>
) {
    val apiKey = context.getString(R.string.api_key)
    val lastWeatherCityKey = context.getString(R.string.last_city_weather_key)
    val filenameWeather = context.getString(R.string.last_city_weather_filename)
    val favoriteCitiesWeatherFilename = context.getString(R.string.favorite_cities_weather)

    if (isNetworkConnectionAvailable(context)) {
        isLoading.value = true
        val result = fetchWeatherData(city.value, apiKey)

        if (result == null) {
            error.value = "City not found"
        } else {
            weatherResponse.value = result
            error.value = null
        }

        isLoading.value = false
        //Saving prefernces
        savePreference(context, lastWeatherCityKey, city.value)

        //Saving weather
        weatherResponse.value?.let {
            saveWeatherData(context, it, filenameWeather)
        }

        // Add current city to favorites if not already there
        var tempFavCities = favoriteCities.value
        if (!favoriteCities.value.contains(city.value)) {
            tempFavCities = favoriteCities.value + city.value
        }

        //Fetching data for favorite list
        weatherList.value = getWeatherForFavorites(tempFavCities, apiKey)

        //Saving weather
        saveFavoriteWeatherList(context, weatherList.value, favoriteCitiesWeatherFilename)
    } else {
        Toast.makeText(
            context,
            "No internet connection, displayed data might not be up to date.",
            Toast.LENGTH_LONG
        ).show()

        //Loading weather for favorites from file
        weatherList.value =
            loadFavoriteWeatherList(context, favoriteCitiesWeatherFilename) ?: emptyList()

        val cityWeather = weatherList.value.find { it.name.equals(city.value, ignoreCase = true) }

        //If is saved then load
        if (cityWeather != null) {
            isLoading.value = true
            weatherResponse.value = cityWeather
            isLoading.value = false
        }
    }
}



