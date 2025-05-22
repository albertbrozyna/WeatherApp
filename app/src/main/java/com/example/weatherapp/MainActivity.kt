package com.example.weatherapp

import android.annotation.SuppressLint
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.weatherapp.ui.theme.WeatherAppTheme
import androidx.compose.ui.Modifier
import com.example.weatherapp.navigation.BottomNavigationBar
import com.example.weatherapp.navigation.BottomNavigationBarTablet
import com.example.weatherapp.pages.SettingsScreen
import com.example.weatherapp.pages.WeatherForecastScreen
import com.example.weatherapp.pages.WeatherScreen
import com.example.weatherapp.utils.isTablet
import androidx.compose.ui.platform.LocalConfiguration
import android.content.res.Configuration

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme(darkTheme = true) {
                val context = LocalContext.current
                val selectedScreen = remember { mutableIntStateOf(0) }
                val configuration = LocalConfiguration.current
                val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
                val tablet = remember { isTablet(context) }

                if (tablet) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBarTablet(selectedScreen ) }) { innerPadding ->
                        when (selectedScreen.intValue) {
                            0 -> WeatherScreen(modifier = Modifier.padding(innerPadding),tablet = true)
                            1 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBar(selectedScreen, isCompact = isLandscape) }) { innerPadding ->
                        when (selectedScreen.intValue) {
                            0 -> WeatherScreen(modifier = Modifier.padding(innerPadding))
                            1 -> WeatherForecastScreen(modifier = Modifier.padding(innerPadding))
                            2 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                }
            }
        }
    }
}










