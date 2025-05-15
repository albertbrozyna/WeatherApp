package com.example.weatherapp.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.weatherapp.utils.ForecastWeather
import com.example.weatherapp.utils.GeoCity
import com.example.weatherapp.R
import com.example.weatherapp.WeatherViewModel
import com.example.weatherapp.utils.CitiesSection
import com.example.weatherapp.utils.ShowFoundCities
import com.example.weatherapp.utils.WeatherForecastList
import com.example.weatherapp.utils.fetchWeatherForecast
import com.example.weatherapp.utils.checkIfCityExists
import com.example.weatherapp.utils.convertTemperatureToF
import com.example.weatherapp.utils.dateWithoutYear
import com.example.weatherapp.utils.getDayName
import com.example.weatherapp.utils.getWeatherForecastForFavorites
import com.example.weatherapp.utils.isNetworkConnectionAvailable
import com.example.weatherapp.utils.loadFavoriteForecastList
import com.example.weatherapp.utils.loadFavouriteCities
import com.example.weatherapp.utils.loadPreferenceJson
import com.example.weatherapp.utils.loadPreferenceString
import com.example.weatherapp.utils.saveFavoriteForecastList
import com.example.weatherapp.utils.savePreferenceJson
import com.example.weatherapp.utils.saveWeatherForecastData
import com.example.weatherapp.utils.searchCitiesByName
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun WeatherForecastScreen(modifier: Modifier = Modifier) {
    val viewModel: WeatherViewModel = viewModel()

    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lastCityForecastKey = context.getString(R.string.last_city_weather_key)

    // Loading last city from preferences
    val lastCityGeoCity = remember {
        mutableStateOf<GeoCity?>(loadPreferenceJson(context, lastCityForecastKey))
    }

    // City in string
    val city = remember {
        mutableStateOf(lastCityGeoCity.value?.name ?: "")
    }

    val weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
    val weatherForecastList = remember { mutableStateOf<List<WeatherForecastList>>(emptyList()) }

    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    val showFavorites = remember { mutableStateOf(false) }

    //List of cities when we are looking
    var cityList = remember { mutableStateOf<List<GeoCity>>(emptyList()) }
    var expanded = remember { mutableStateOf(false) }
    var lat = remember { mutableFloatStateOf(0.0f) }
    var lon = remember { mutableFloatStateOf(0.0f) }

    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    val currentCityShowed = remember { mutableStateOf("") }

    //Refresh key and interval

    val refreshTimeKey = context.getString(R.string.refresh_time_key)
    val refreshInterval = loadPreferenceString(context, refreshTimeKey)?.toLongOrNull() ?: 5L

    //Reloading UI
    val reload = remember { mutableStateOf(false) }

    LaunchedEffect(reload.value) {
        updateWeatherForecast(
            context,
            isLoading,
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

    LaunchedEffect(lat.floatValue, lon.floatValue) {
        if (city.value.isNotEmpty()){ // If we are searching a city

            updateWeatherForecast(
                context,
                isLoading,
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


    // Selecting background depending on a hour
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
            scope.launch {

                updateWeatherForecast(
                    context,
                    isLoading,
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

        onDispose {
            viewModel.pauseAutoRefresh()
        }
    }

    // Loading started data
    LaunchedEffect(Unit) {
        updateWeatherForecast(
            context,
            isLoading,
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
                .fillMaxWidth()
                .padding(16.dp)
                .verticalScroll(scroll),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CitiesSection(favoriteCities, showFavorites, context, reload, expanded, city, lat, lon)

            //Button to get forecast weather
            Button(
                onClick = {
                    scope.launch {
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
                Text("Get weather forecast", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }

            // Dropdown menu with available cities
            ShowFoundCities(cityList.value, expanded, city, lat, lon)


            if (isLoading.value) {
                Box(
                    modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "Loading...", textAlign = TextAlign.Center, fontSize = 30.sp
                    )
                }
            } else {
                weatherForecast.value?.let {
                    WeekDaysForecast(context, currentCityShowed.value, it)
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
fun WeekDaysForecast(context: Context, city: String, weatherForecast: WeatherForecastList) {
    val selectedDates =
        weatherForecast.list.filter { it.dt_txt.contains(context.getString(R.string.forecast_hour)) }
            .groupBy { it.dt_txt.substring(0, 10) }.toList().take(7)

    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        //City name
        Text(
            text = city,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            fontSize = 40.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(10.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            selectedDates.forEach { (date, forecasts) ->
                val firstForecast = forecasts.first()
                DayView(context, date, firstForecast)
            }
        }
    }
}

//One day view
@Composable
fun DayView(context: Context, date: String, forecast: ForecastWeather) {

    val dayName = getDayName(date)
    val iconCode = forecast.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    val weatherDesc = forecast.weather.firstOrNull()?.description


    //keys
    val tempUnitsKey = context.getString(R.string.temp_units_key)

    //units preferences
    val tempUnits = loadPreferenceString(context, tempUnitsKey) ?: "metric"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(color = Color(0x66000000)), contentAlignment = Alignment.Center

    ) {

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(16.dp))

            Text(
                text = dayName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(8.dp))

            //Small date under
            Text(
                dateWithoutYear(date),
                fontSize = 16.sp,
                fontWeight = FontWeight.Normal,
                textAlign = TextAlign.Center
            )

            Spacer(Modifier.height(10.dp))

            //Temp

            val temp = if (tempUnits == "metric") {
                "${forecast.main.temp.toInt()}°C"
            } else {
                convertTemperatureToF(forecast.main.temp)
            }

            Text(temp, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            //Weather icon-
            iconCode?.let {

                AsyncImage(
                    model = iconUrl,
                    contentDescription = "Weather Icon",
                    modifier = Modifier.size(100.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            weatherDesc?.let {
                Text(
                    weatherDesc,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Normal,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}

suspend fun updateWeatherForecast(
    context: Context,
    isLoading: MutableState<Boolean>,
    city: MutableState<String>,
    weatherForecast: MutableState<WeatherForecastList?>,
    error: MutableState<String?>,
    favoriteCities: MutableState<List<GeoCity>>,
    weatherForecastList: MutableState<List<WeatherForecastList>>,
    currentCityShowed: MutableState<String>,
    lat: MutableState<Float>,          // Added a lat and lot to searching
    lon: MutableState<Float>
) {

    val apiKey = context.getString(R.string.api_key)
    val lastCityForecastKey = context.getString(R.string.last_city_weather_key)
    val filenameForecast = context.getString(R.string.last_city_forecast)

    if (city.value.trim().isEmpty()) {   // Is city field is empty exit
        return
    }

    if (isNetworkConnectionAvailable(context)) {    // If connection is available fetch data
        isLoading.value = true
        val result = fetchWeatherForecast(lat = lat.value, lon = lon.value, apiKey)

        if (result != null) {
            weatherForecast.value = result
            currentCityShowed.value = city.value
            saveWeatherForecastData(context, result, filenameForecast)

            error.value = null
        } else {
            error.value = "City not found"
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

                // Saving last city as GeoCity
                savePreferenceJson(context, lastCityForecastKey, result)
            }
        }

        //Fetching data for favorite list
        weatherForecastList.value = getWeatherForecastForFavorites(tempFavCities, apiKey)

        //Saving weather
        saveFavoriteForecastList(context, weatherForecastList.value, filenameForecast)
    } else {
        Toast.makeText(
            context,
            "No internet connection, displayed data might not be up to date.",
            Toast.LENGTH_SHORT
        ).show()

        //Loading forecast data from file
        weatherForecastList.value =
            loadFavoriteForecastList(context, filenameForecast) ?: emptyList()

        // Find if there is a city with this coord
        val cityForecast = weatherForecastList.value.find {
            it.city.coord.lon.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) ==
                    lon.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) &&
                    it.city.coord.lat.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP) ==
                    lat.value.toBigDecimal().setScale(4, java.math.RoundingMode.HALF_UP)
        }

        // Updating forecast if we can
        if (cityForecast != null) {
            isLoading.value = true
            weatherForecast.value = cityForecast
            currentCityShowed.value = city.value
            isLoading.value = false
        }
    }

}







