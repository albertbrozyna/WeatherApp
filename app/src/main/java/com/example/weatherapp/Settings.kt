package com.example.weatherapp

import android.content.Context
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SettingsScreen(modifier: Modifier = Modifier){
    val context: Context = LocalContext.current

    //Keys
    val refreshTimeKey = context.getString(R.string.refresh_time_key)
    val windUnitsKey = context.getString(R.string.temp_units_key)
    val tempUnitsKey = context.getString(R.string.wind_units_key)

    //Refresh time interval default 60 s
    val refreshInterval = remember { mutableStateOf<Int>(
        loadPreference(context,refreshTimeKey)?.toInt() ?: 60)  }
    val windUnits = remember { mutableStateOf(loadPreference(context,windUnitsKey) ?: "km/h")}
    val tempUnits = remember { mutableStateOf(loadPreference(context,refreshTimeKey) ?: "metric")}


    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {


        Text("Select Temperature Unit", fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        //Temp units select
        Row(modifier = Modifier.fillMaxWidth()) {
            //Metric system
            RadioButton(
                selected = tempUnits.value == "metric",
                onClick = {
                    tempUnits.value = "metric"
                    savePreference(context, tempUnitsKey, "metric")
                }
            )
            Text("Celsius")

            Spacer(Modifier.width(16.dp))

            RadioButton(
                selected = tempUnits.value == "imperial",
                onClick = {
                    tempUnits.value = "imperial"
                    savePreference(context, tempUnitsKey, "imperial")
                }
            )
            Text("Fahrenheit")
        }

        Spacer(modifier = Modifier.height(20.dp))

        //Wind units
        Text("Select Wind Speed Unit", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )

        Row(modifier = Modifier.fillMaxWidth()) {
            RadioButton(
                selected = windUnits.value == "km/h",
                onClick = {
                    windUnits.value = "km/h"
                    savePreference(context, windUnitsKey, "km/h")
                }
            )
            Text("km/h")


            Spacer(Modifier.width(16.dp))

            RadioButton(
                selected = windUnits.value == "m/s",
                onClick = {
                    windUnits.value = "m/s"
                    savePreference(context, windUnitsKey, "m/s")
                }
            )
            Text("m/s")
        }

        //Refresh time
        Text("Select Refresh Interval", fontSize = 20.sp, fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(vertical = 16.dp)
        )
        Row() {
            RadioButton(
                selected = refreshInterval.value == 5,
                onClick = {
                    refreshInterval.value = 5
                    savePreference(context, refreshTimeKey, 5.toString())
                }
            )
            Text("5 minutes")


            Spacer(Modifier.width(16.dp))
            RadioButton(
                selected = refreshInterval.value == 30,
                onClick = {
                    refreshInterval.value = 30
                    savePreference(context, refreshTimeKey, 30.toString())
                }
            )
            Text("30 minutes")

            Spacer(Modifier.width(16.dp))
            RadioButton(
                selected = refreshInterval.value == 60,
                onClick = {
                    refreshInterval.value = 60
                    savePreference(context, refreshTimeKey, 60.toString())
                }
            )
            Text("60 minutes")
        }


    }

}
