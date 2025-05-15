package com.example.weatherapp.utils

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.weatherapp.R
import kotlinx.coroutines.launch
import androidx.compose.ui.text.TextStyle as TxtStyle

@Composable
fun CitiesSection(
    favoriteCities: MutableState<List<GeoCity>>,
    showFavorites: MutableState<Boolean>,
    context: Context,
    reload: MutableState<Boolean>,
    expanded: MutableState<Boolean>,
    city: MutableState<String>,
    lat : MutableState<Float>,
    lon : MutableState<Float>
) {
    val coroutineScope = rememberCoroutineScope()


    Column(
        modifier = Modifier.fillMaxWidth(),
    ) {
        //Favorities list
        if (showFavorites.value) {
            Text(
                "Favourite cities:", modifier = Modifier.padding(8.dp)
            )

            favoriteCities.value.forEach { cityItem->

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {

                    TextButton(
                        // Changing lat and lot and city name
                        onClick = {
                            city.value = cityItem.name
                            lat.value = cityItem.lat
                            lon.value = cityItem.lon
                        }) {
                        Text(
                            buildString {
                                append(" - ${cityItem.name}")
                                append(" (${cityItem.country}")
                                cityItem.state?.let { append(", $it") }
                                append(")")
                            },
                            modifier = Modifier.padding(start = 8.dp),
                            textAlign = TextAlign.Start,
                            maxLines = 2,
                            fontSize = 16.sp
                        )
                    }
                    //Icon button do delete from favorite list
                    IconButton(
                        onClick = {
                            val updatedList = favoriteCities.value.toMutableList().apply {
                                remove(cityItem)
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

            // Text field to input cities
            TextField(
                value = city.value,
                onValueChange = { city.value = it },
                label = { Text("City", fontSize = 12.sp, fontWeight = FontWeight.SemiBold) },
                textStyle = TxtStyle(fontSize = 20.sp),
                modifier = Modifier
                    .weight(7f)
                    .padding(top = 6.dp)
                    .onFocusChanged { focusState -> // If we lost focus list is not showing
                        if (!focusState.isFocused) {
                            expanded.value = false
                        }
                    },
                shape = RoundedCornerShape(6.dp),
                singleLine = true
            )

            // Button for adding to favourites
            IconButton(
                onClick = {
                    val cityName = city.value.trim()
                    if (cityName.isEmpty()){
                        return@IconButton
                    }

                    // Checking if city exists in fav
                    val existsInFavourites = favoriteCities.value.any { fav ->
                        fav.lat == lat.value && fav.lon == lon.value
                    }

                    if (existsInFavourites) {
                        Toast.makeText(context, "Current city is already in favourites.", Toast.LENGTH_LONG).show()
                        return@IconButton
                    }

                    // Checking if we have a internet connection, if not we can't add a city to fav
                    if (!isNetworkConnectionAvailable(context)) {
                        Toast.makeText(context, "No internet connection. Cannot add city.", Toast.LENGTH_LONG).show()
                        return@IconButton
                    }

                    coroutineScope.launch {
                        // Check if lon and lat are correct and city exists
                        val result = checkIfCityExists(context, lon = lon.value, lat = lat.value)

                        if (result == null) {
                            Toast.makeText(
                                context,
                                "City not found with those coordinates.",
                                Toast.LENGTH_LONG
                            ).show()
                            return@launch
                        }

                        // Add city to favorites
                        val updatedList = favoriteCities.value.toMutableList().apply {
                            add(result)
                        }

                        favoriteCities.value = updatedList
                        saveFavouriteCities(context, updatedList)

                        Toast.makeText(context, "${result.name} added to favourites.", Toast.LENGTH_LONG).show()
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