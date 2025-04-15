package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import kotlinx.coroutines.launch
import java.time.LocalTime

@Composable
fun WeatherForecastScreen(modifier: Modifier = Modifier) {
    val context: Context = LocalContext.current
    val lastCityForecastKey = context.getString(R.string.last_city_forecast)
    var city = remember { mutableStateOf(loadPreference(context, lastCityForecastKey) ?: "") }
    var weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
   val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    var showFavorites = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val filenameForecast = context.getString(R.string.weather_forecast_data)
    val apiKey = context.getString(R.string.api_key)
    val currentHour = remember { LocalTime.now().hour }
    val reload = remember { mutableStateOf(false) }

    //Reloading UI
    LaunchedEffect(reload.value) {
        if (isNetworkConnectionAvailable(context)) {

            isLoading.value = true
            val result = fetchWeatherForecast(city.value, apiKey)

            if (result == null) {
                weatherForecast.value = null
                error.value = "City not found"
            } else {
                weatherForecast.value = result
                error.value = null
            }

            isLoading.value = false

            //saving last city state
            savePreference(context, lastCityForecastKey, city.value)

            //Saving data forecast data
            weatherForecast.value?.let {
                saveWeatherForecastData(
                    context, it, filenameForecast
                )
            }
            reload.value = false
        } else {
            weatherForecast.value =
                loadWeatherForecastData(context, filenameForecast)
            Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG)
                .show()
        }
    }

    //Selecting background
    val backgroundImage = when (currentHour) {
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
    }

    //Loading started data
    LaunchedEffect(Unit) {
        if (city.value.isNotEmpty()) {
            weatherForecast.value = loadWeatherForecastData(context, filenameForecast)
        }

        //Checking internet connection on start
        if (!isNetworkConnectionAvailable(context)){
            Toast.makeText(context, "No internet connection, displayed data might not be up to date.", Toast.LENGTH_LONG).show()
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

        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            CitiesSection(favoriteCities, showFavorites, city, context,reload)

            Button(
                onClick = {
                    scope.launch {
                        if (isNetworkConnectionAvailable(context)) {

                            isLoading.value = true
                            val result = fetchWeatherForecast(city.value, apiKey)

                            if (result == null) {
                                weatherForecast.value = null
                                error.value = "City not found"
                            } else {
                                weatherForecast.value = result
                                error.value = null
                            }

                            isLoading.value = false

                            //saving last city state
                            savePreference(context ,lastCityForecastKey,city.value)

                            //Saving data forecast data
                            weatherForecast.value?.let {
                                saveWeatherForecastData(
                                    context, it, filenameForecast
                                )
                            }

                        } else {
                            weatherForecast.value =
                                loadWeatherForecastData(context, filenameForecast)
                            Toast.makeText(context, "No internet connection.", Toast.LENGTH_LONG)
                                .show()
                        }

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
            }else{
                weatherForecast.value?.let {
                    WeekDaysForecast(context, it)
                }
            }
        }
    }

}


@Composable
fun WeekDaysForecast(context: Context, weatherForecast: WeatherForecastList) {
    val selectedDates =
        weatherForecast.list.filter { it.dt_txt.contains(context.getString(R.string.forecast_hour)) }
            .groupBy { it.dt_txt.substring(0, 10) }.toList().take(7)

    val scroll = rememberScrollState()

    Column(
        modifier = Modifier.fillMaxWidth().verticalScroll(scroll),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        selectedDates.forEach { (date, forecasts) ->

            val firstForecast = forecasts.first()

            DayView(context,date, firstForecast)

            Spacer(modifier = Modifier.height(16.dp))
        }
    }


}

//One day view
@Composable
fun DayView(context :Context,date: String, forecast: ForecastWeather) {

    val dayName = getDayName(date)
    val iconCode = forecast.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    val weatherDesc = forecast.weather.firstOrNull()?.description


    //keys
    val tempUnitsKey = context.getString(R.string.temp_units_key)

    //units preferences
    val tempUnits = loadPreference(context, tempUnitsKey) ?: "metric"

    Box(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)).background(color = Color(0x66000000)),
        contentAlignment = Alignment.Center
    ){

        Column(modifier = Modifier.fillMaxWidth().clip(RoundedCornerShape(20.dp)), horizontalAlignment = Alignment.CenterHorizontally) {

            Spacer(Modifier.height(16.dp))

            Text(text = dayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

            Spacer(Modifier.height(8.dp))

            //Small date under
            Text(dateWithoutYear(date),fontSize = 16.sp, fontWeight = FontWeight.Normal,textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            //Temp

            val temp = if( tempUnits == "metric"){
                "${forecast.main.temp.toInt()}°C"
            }else{
                convertTemperatureToF(forecast.main.temp)
            }

            Text(temp,fontSize = 24.sp, fontWeight = FontWeight.Bold,textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            //Weather icon-
            iconCode?.let {

                AsyncImage(
                    model = iconUrl,
                    contentDescription = "Weather Icon",
                    modifier = Modifier
                        .size(100.dp)
                )
            }

            Spacer(Modifier.height(10.dp))

            weatherDesc?.let {
                Text(weatherDesc,fontSize = 24.sp, fontWeight = FontWeight.Normal,textAlign = TextAlign.Center)
            }

            Spacer(Modifier.height(16.dp))
        }
    }
}









