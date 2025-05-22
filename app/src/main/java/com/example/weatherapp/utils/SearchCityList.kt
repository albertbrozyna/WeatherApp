package com.example.weatherapp.utils

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier

@Composable
fun ShowFoundCities(
    cities: List<GeoCity>,
    expanded: MutableState<Boolean>,
    selectedCity: MutableState<String>,
    lat: MutableState<Float>,
    lon: MutableState<Float>
) {
    Box(modifier = Modifier.fillMaxWidth()) {

        DropdownMenu(
            expanded = expanded.value,
            onDismissRequest = {
                expanded.value = false
            },
            modifier = Modifier.fillMaxWidth()
        ) {

            if(cities.isEmpty()){
                DropdownMenuItem(
                    onClick = {},
                    text = {
                        Text("No cities with this name found")
                    }
                )
            }

            cities.forEach { city ->
                DropdownMenuItem(
                    onClick = {
                        selectedCity.value = city.name
                        lon.value = city.lon
                        lat.value = city.lat
                        expanded.value = false
                    },
                    text = {
                        Text(
                            "${city.name} - Country: ${city.country}" +
                                    (city.state?.let { ", State: $it" } ?: "")
                        )
                    }
                )
            }
        }
    }
}



