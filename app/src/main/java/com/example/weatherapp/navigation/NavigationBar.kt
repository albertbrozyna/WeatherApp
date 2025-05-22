package com.example.weatherapp.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.weatherapp.R

@Composable
fun BottomNavigationBar(selectedScreen: MutableState<Int>, isCompact: Boolean) {
    val iconSize = if (isCompact) 20.dp else 28.dp
    val padding = if (isCompact) 8.dp else 16.dp

    NavigationBar(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        NavigationBarItem(
            onClick = { selectedScreen.value = 0 },
            selected = selectedScreen.value == 0,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home, contentDescription = "Home",
                    modifier = Modifier.size(iconSize)
                )
            },
            label = { Text("Home") }

        )
        NavigationBarItem(onClick = { selectedScreen.value = 1 }, icon = {
            Icon(
                painter = painterResource(id = R.drawable.baseline_sunny_24),
                contentDescription = "Weather forecast",modifier = Modifier.size(iconSize)
            )
        }, selected = selectedScreen.value == 1, label = { Text("Weather forecast") })


        NavigationBarItem(onClick = { selectedScreen.value = 2 }, icon = {
            Icon(
                imageVector = Icons.Default.Settings, contentDescription = "Settings", modifier = Modifier.size(iconSize)
            )

        }, selected = selectedScreen.value == 2, label = { Text("Settings") })
    }
}