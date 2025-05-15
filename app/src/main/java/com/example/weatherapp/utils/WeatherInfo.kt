package com.example.weatherapp.utils

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.R


@Composable
fun WeatherInfo(context: Context, weatherResponse: WeatherResponse) {
    //variables to icon
    val iconCode = weatherResponse.weather.firstOrNull()?.icon
    val iconUrl = "https://openweathermap.org/img/wn/$iconCode@2x.png"

    //keys
    val windUnitsKey = context.getString(R.string.wind_units_key)
    val tempUnitsKey = context.getString(R.string.temp_units_key)

    //units preferences
    val windUnits = loadPreferenceString(context, windUnitsKey) ?: "m/s"
    val tempUnits = loadPreferenceString(context, tempUnitsKey) ?: "metric"

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