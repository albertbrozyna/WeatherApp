package com.example.weatherapp

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.modifier.ModifierLocalConsumer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import kotlinx.coroutines.launch
import retrofit2.http.GET
import retrofit2.http.Query
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.time.LocalTime
import java.util.Locale

@Composable
fun WeatherForecastScreen(modifier: Modifier = Modifier) {
    var city = remember { mutableStateOf("") }
    var weatherForecast = remember { mutableStateOf<WeatherForecastList?>(null) }
    val context: Context = LocalContext.current
    val favoriteCities = remember { mutableStateOf(loadFavouriteCities(context)) }
    var showFavorites = remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    var isLoading = remember { mutableStateOf(false) }
    val error = remember { mutableStateOf<String?>(null) }
    val filenameForecast = context.getString(R.string.weather_forecast_data)
    val apiKey = context.getString(R.string.api_key)
    val currentHour = remember { LocalTime.now().hour }

    val backgroundImage = when (currentHour) {
        in 6..20 -> R.drawable.sky
        in 21..24 -> R.drawable.night
        in 0..5 -> R.drawable.night
        else -> R.drawable.sky
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
            CitiesSection(favoriteCities, showFavorites, city, context)


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

                            //Saving data
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


            weatherForecast.value?.let {
                WeekDaysForecast(context, it)
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
        selectedDates.forEach { (date, forecasts) ->

            val firstForecast = forecasts.first()

            DayView(date, firstForecast)
        }
    }


}

//One day view
@Composable
fun DayView(date: String, forecast: ForecastWeather) {

    val dayName = getDayName(date)
    val iconCode = forecast.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/${iconCode}@2x.png"
    val weatherDesc = forecast.weather.firstOrNull()?.description

    Box(){

        Column(modifier = Modifier.fillMaxWidth()) {

            Text(text = dayName, fontSize = 24.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)

            Spacer(Modifier.width(8.dp))

            //Small date under
            Text(dateWithoutYear(date),fontSize = 16.sp, fontWeight = FontWeight.Normal,textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            //Temp
            Text("${forecast.main.temp.toInt()}°C",fontSize = 24.sp, fontWeight = FontWeight.Bold,textAlign = TextAlign.Center)

            Spacer(Modifier.height(10.dp))

            //Weather icon-
            iconCode?.let {

                AsyncImage(
                    model = iconUrl,
                    contentDescription = "Weather Icon",
                    modifier = Modifier
                        .size(120.dp)
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


data class WeatherForecastList(
    val list: List<ForecastWeather>
)

data class ForecastWeather(
    val dt: Long, val main: Main, val weather: List<Weather>, val dt_txt: String
)

interface WeatherForecastAPI {
    @GET("data/2.5/forecast")
    suspend fun getWeatherForecastByCity(
        @Query("q") city: String,
        @Query("appid") apiKey: String,
        @Query("units") units: String = "metric"
    ): WeatherForecastList
}

//Getting forecast for next days
suspend fun fetchWeatherForecast(city: String, apiKey: String): WeatherForecastList? {
    return try {
        val weatherForecastAPI = WeatherApiClient.weatherForecastAPI
        weatherForecastAPI.getWeatherForecastByCity(city, apiKey)

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

//Saving forecast data to file
fun saveWeatherForecastData(
    context: Context,
    weatherForecast: WeatherForecastList,
    filename: String
) {
    try {
        val file = File(context.filesDir, filename)

        val fileOutputStream = FileOutputStream(file)

        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(weatherForecast)
        objectOutputStream.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

//Reading forecast data from file
fun loadWeatherForecastData(context: Context, filename: String): WeatherForecastList? {

    try {
        val file = File(context.filesDir, filename)

        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val weatherForecast = objectInputStream.readObject() as WeatherForecastList
            objectInputStream.close()
            return weatherForecast

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}


fun getDayName(date: String): String {
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    val parsedDate = dateFormat.parse(date)

    if (parsedDate == null) {
        return "Invalid date"
    }

    val dayFormat = SimpleDateFormat("EEEE", Locale.ENGLISH)
    return dayFormat.format(parsedDate)
}

fun dateWithoutYear(date: String): String {
    val dateFormat = SimpleDateFormat("MM-dd", Locale.ENGLISH)

    val parsedDate = dateFormat.parse(date)

    if (parsedDate == null) {
        return "Invalid date"
    }
    return parsedDate.toString()
}