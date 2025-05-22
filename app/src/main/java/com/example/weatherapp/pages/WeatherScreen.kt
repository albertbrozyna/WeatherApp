package com.example.weatherapp.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.utils.CitiesSection
import com.example.weatherapp.utils.GeoCity
import com.example.weatherapp.utils.ShowFoundCities
import com.example.weatherapp.utils.WeatherForecastList
import com.example.weatherapp.utils.WeatherInfo
import com.example.weatherapp.utils.WeatherResponse
import com.example.weatherapp.utils.checkIfCityExists
import com.example.weatherapp.utils.fetchWeatherData
import com.example.weatherapp.utils.getWeatherForFavorites
import com.example.weatherapp.utils.isNetworkConnectionAvailable
import com.example.weatherapp.utils.loadFavoriteWeatherList
import com.example.weatherapp.utils.loadFavouriteCities
import com.example.weatherapp.utils.loadPreferenceJson
import com.example.weatherapp.utils.loadPreferenceString
import com.example.weatherapp.utils.saveFavoriteWeatherList
import com.example.weatherapp.utils.savePreferenceJson
import com.example.weatherapp.utils.searchCitiesByName
import kotlinx.coroutines.launch
import java.time.LocalTime
import kotlin.collections.plus

@Composable
fun WeatherScreen(modifier: Modifier = Modifier, tablet: Boolean = false) {
    val viewModel: WeatherViewModel = viewModel()

    val context: Context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    // Name where the last city (GeoCity type) is stored
    val lastWeatherCityKey = context.getString(R.string.last_city_weather_key)

    // Loading last city from preferences
    val lastCityGeoCity = remember {
        mutableStateOf<GeoCity?>(loadPreferenceJson(context, lastWeatherCityKey))
    }

    // City in string
    val city = remember {
        mutableStateOf(lastCityGeoCity.value?.name ?: "")
    }

    val weatherResponse = remember { mutableStateOf<WeatherResponse?>(null) }

    // List of weather for favorite cities
    val weatherList = remember { mutableStateOf<List<WeatherResponse>>(emptyList()) }
    // List of favourite cities
    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }

    val isLoading = remember { mutableStateOf(false) }
    val showFavorites = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }


    //List of cities when we are looking
    var cityList =  remember { mutableStateOf<List<GeoCity>>(emptyList()) }
    var expanded = remember { mutableStateOf(false) }
    var lat = remember { mutableFloatStateOf(lastCityGeoCity.value?.lat ?: 0.0f) }
    var lon = remember { mutableFloatStateOf(lastCityGeoCity.value?.lon ?: 0.0f) }

    //Keys
    val refreshTimeKey = context.getString(R.string.refresh_time_key)
    val refreshInterval = loadPreferenceString(context, refreshTimeKey)?.toLongOrNull() ?: 60L

    //For forecast for tablet

    val weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
    val weatherForecastList = remember { mutableStateOf<List<WeatherForecastList>>(emptyList()) }

    val currentCityShowed = remember { mutableStateOf("") }

    var unsued = remember { mutableStateOf<Boolean>(false) }


    // Fetching weather for city
    LaunchedEffect(lat.floatValue,lon.floatValue) {
        if (city.value.isNotEmpty()){ // If we are searching a city
            updateWeather(context, isLoading, city, weatherResponse, error, favoriteCities, weatherList,lat,lon)

            updateWeatherForecast(
                context,
                unsued,
                city,
                weatherForecast,
                error,
                favoriteCities,
                weatherForecastList,
                currentCityShowed,
                lat,
                lon
            )
        }
    }

    // Reloading
    val reload = remember { mutableStateOf(false) }

    LaunchedEffect(reload.value) {
        updateWeather(context, isLoading, city, weatherResponse, error, favoriteCities, weatherList,lat,lon)

        updateWeatherForecast(
            context,
            unsued,
            city,
            weatherForecast,
            error,
            favoriteCities,
            weatherForecastList,
            currentCityShowed,
            lat,
            lon
        )
    }

    //Selecting background depending on hour
    val currentHour = remember { LocalTime.now().hour }

    val backgroundImage = when (currentHour) {
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
    }

    // Updating weather every time interval
    DisposableEffect(Unit) {
        val refreshIntervalSec = refreshInterval * 1000 // To seconds
        viewModel.startAutoRefreshTimer(context, refreshIntervalSec) {
            coroutineScope.launch {
                updateWeather(
                    context, isLoading, city, weatherResponse, error, favoriteCities, weatherList, lat, lon
                )
                updateWeatherForecast(
                    context, unsued, city, weatherForecast, error,
                    favoriteCities, weatherForecastList, currentCityShowed, lat, lon
                )
            }
        }

        onDispose {
            viewModel.pauseAutoRefresh()
        }
    }

    //Loading on start
    LaunchedEffect(Unit) {
        // Loading last city
        if (city.value.isNotEmpty()) {
            updateWeather(
                context, isLoading, city, weatherResponse, error, favoriteCities, weatherList,lat,lon
            )

            updateWeatherForecast(
                context,
                unsued,
                city,
                weatherForecast,
                error,
                favoriteCities,
                weatherForecastList,
                currentCityShowed,
                lat,
                lon
            )
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

            CitiesSection(favoriteCities, showFavorites, context, reload,expanded,city,lat,lon)

            //Button to get weather
            Button(
                onClick = {
                    coroutineScope.launch {
                        // Fetch available cities
                        cityList.value =
                            searchCitiesByName(context = context, cityName = city.value)

                        // Show menu with cities
                        expanded.value = true
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, top = 8.dp, end = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Get Weather", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            // Dropdown menu with available cities

            ShowFoundCities(cityList.value,expanded,city,lat, lon)

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

suspend fun updateWeather(
    context: Context,
    isLoading: MutableState<Boolean>,
    city: MutableState<String>,
    weatherResponse: MutableState<WeatherResponse?>,
    error: MutableState<String?>,
    favoriteCities: MutableState<List<GeoCity>>,
    weatherList: MutableState<List<WeatherResponse>>,
    lat: MutableState<Float>,          // Added a lat and lot to searching
    lon: MutableState<Float>
) {
    val apiKey = context.getString(R.string.api_key)
    val lastWeatherCityKey = context.getString(R.string.last_city_weather_key)
    val favoriteCitiesWeatherFilename = context.getString(R.string.favorite_cities_weather)

    if(city.value.trim().isEmpty()){   // Is city field is empty exit
        return
    }

    if (isNetworkConnectionAvailable(context)) {
        isLoading.value = true

        // Fetching weather from internet
        val result = fetchWeatherData(lat = lat.value, lon = lon.value, apiKey = apiKey)

        if (result == null) {
            error.value = "City not found"
        } else {
            weatherResponse.value = result
            error.value = null
        }

        isLoading.value = false


        // Creating a tempFavCities only for fetching weather for the last city
        var tempFavCities = favoriteCities.value

        // Add current city to favorites, if not exists to store the weather here for the last city
        val existsInFavourites = favoriteCities.value.any { fav ->
            fav.lat == lat.value && fav.lon == lon.value
        }

        if (!existsInFavourites) {   // If don't exists in fav

            val result = checkIfCityExists(context, lon = lon.value, lat = lat.value)
            if (result != null) { // If exists add it to temp list
                tempFavCities = favoriteCities.value + result

                // Save this city name as last to preferences

                savePreferenceJson(context, lastWeatherCityKey, result)
            }
        }

        //Fetching data for favorite list
        weatherList.value = getWeatherForFavorites(tempFavCities, apiKey)

        // Saving weather
        saveFavoriteWeatherList(context, weatherList.value, favoriteCitiesWeatherFilename)
    } else {
        Toast.makeText(
            context,
            "No internet connection, displayed data might not be up to date.",
            Toast.LENGTH_LONG
        ).show()

        // Loading weather for favorites from file
        weatherList.value =
            loadFavoriteWeatherList(context, favoriteCitiesWeatherFilename) ?: emptyList()

        // Find if there is a city with this coord
        val cityWeather = weatherList.value.find {
            it.coord.lon.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) ==
                    lon.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) &&
                    it.coord.lat.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) ==
                    lat.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)
        }

        // If is saved then load
        if (cityWeather != null) {
            isLoading.value = true
            weatherResponse.value = cityWeather
            isLoading.value = false

            val matchedCity = favoriteCities.value.find  { fav ->
                fav.lat.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)  == lat.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)
                && fav.lon.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)== lon.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)
            }

            if (matchedCity != null) {
                savePreferenceJson(context, lastWeatherCityKey, matchedCity)
            }
        }
    }
}
