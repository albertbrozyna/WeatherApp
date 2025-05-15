package com.example.weatherapp


import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.unit.dp

@Composable
fun BottomNavigationBarTablet(selectedScreen: MutableState<Int>) {

    NavigationBar(
        modifier = Modifier.padding(top = 16.dp),
    ) {
        NavigationBarItem(
            onClick = { selectedScreen.value = 0 },
            selected = selectedScreen.value == 0,
            icon = {
                Icon(
                    imageVector = Icons.Default.Home, contentDescription = "Home"
                )
            },
            label = { Text("Home") }

        )

        NavigationBarItem(onClick = { selectedScreen.value = 1 }, icon = {
            Icon(
                imageVector = Icons.Default.Settings, contentDescription = "Settings"
            )

        }, selected = selectedScreen.value == 1, label = { Text("Settings") })
    }
}