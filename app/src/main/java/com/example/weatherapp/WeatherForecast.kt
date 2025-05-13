package com.example.weatherapp

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
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
import coil.compose.AsyncImage
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun WeatherForecastScreen(modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    val scope = rememberCoroutineScope()

    val lastCityForecastKey = context.getString(R.string.last_city_weather_key)
    val city = remember { mutableStateOf(loadPreference(context, lastCityForecastKey) ?: "") }

    val weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
    val weatherForecastList = remember { mutableStateOf<List<WeatherForecastList>>(emptyList()) }

    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    val showFavorites = remember { mutableStateOf(false) }

    var expanded = remember { mutableStateOf(false) }

    val isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }

    val currentCityShowed = remember { mutableStateOf("") }


    //Refresh key and interval

    val refreshTimeKey = context.getString(R.string.refresh_time_key)
    val refreshIntervalMinutes = loadPreference(context, refreshTimeKey)?.toIntOrNull() ?: 5L

    //Delay
    LaunchedEffect(city.value) {
        val delayTime = (refreshIntervalMinutes.toLong() * 60L * 1000L)
        while (true) {
            delay(delayTime)
            updateWeatherForecast(
                context,
                isLoading,
                city,
                weatherForecast,
                error,
                favoriteCities,
                weatherForecastList,
                currentCityShowed
            )
        }
    }

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
            currentCityShowed
        )
    }

    //Selecting background
    val currentHour = remember { LocalTime.now().hour }

    val backgroundImage = when (currentHour) {
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
    }

    //Loading started data
    LaunchedEffect(Unit) {
        updateWeatherForecast(
            context,
            isLoading,
            city,
            weatherForecast,
            error,
            favoriteCities,
            weatherForecastList,
            currentCityShowed
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
            CitiesSection(favoriteCities, showFavorites, city, context, reload,expanded)

            //Button to get forecast weather
            Button(
                onClick = {
                    scope.launch {
                        updateWeatherForecast(
                            context,
                            isLoading,
                            city,
                            weatherForecast,
                            error,
                            favoriteCities,
                            weatherForecastList,
                            currentCityShowed
                        )
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 6.dp, top = 8.dp, end = 6.dp),
                shape = RoundedCornerShape(6.dp)
            ) {
                Text("Get weather forecast", fontSize = 20.sp, fontWeight = FontWeight.SemiBold)
            }


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
    val tempUnits = loadPreference(context, tempUnitsKey) ?: "metric"

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
    favoriteCities: MutableState<List<String>>,
    weatherForecastList: MutableState<List<WeatherForecastList>>,
    currentCityShowed: MutableState<String>
) {

    val apiKey = context.getString(R.string.api_key)
    val lastCityForecastKey = context.getString(R.string.last_city_weather_key)
    val filenameForecast = context.getString(R.string.last_city_forecast)

    if (city.value.isNotEmpty()) {
        if (isNetworkConnectionAvailable(context)) {
            isLoading.value = true
            val result = fetchWeatherForecast(city.value, apiKey)

            if (result != null) {
                weatherForecast.value = result
                currentCityShowed.value = city.value
                saveWeatherForecastData(context, result, filenameForecast)
                savePreference(context, lastCityForecastKey, city.value)
                error.value = null
            } else {
                error.value = "City not found"
            }
            isLoading.value = false
            // Add current city to save
            var tempFavCities = favoriteCities.value
            if (!favoriteCities.value.contains(city.value)) {
                tempFavCities = favoriteCities.value + city.value
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

            val cityForecast = weatherForecastList.value.find {
                it.city.name.equals(
                    city.value, ignoreCase = true
                )
            }

            if (cityForecast != null) {
                isLoading.value = true
                weatherForecast.value = cityForecast
                currentCityShowed.value = city.value
                isLoading.value = false
            }
        }
    }
}







