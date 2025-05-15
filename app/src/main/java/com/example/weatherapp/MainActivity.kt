package com.example.weatherapp

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.weatherapp.ui.theme.WeatherAppTheme
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.time.LocalTime
import androidx.compose.ui.text.TextStyle as TxtStyle
import androidx.compose.ui.Modifier
import com.example.weatherapp.navigation.BottomNavigationBar
import com.example.weatherapp.navigation.BottomNavigationBarTablet
import com.example.weatherapp.pages.SettingsScreen
import com.example.weatherapp.pages.WeatherForecastScreen
import com.example.weatherapp.pages.WeatherScreen
import com.example.weatherapp.pages.WeekDaysForecast
import com.example.weatherapp.pages.updateWeatherForecast
import com.example.weatherapp.utils.GeoCity
import com.example.weatherapp.utils.WeatherForecastList
import com.example.weatherapp.utils.WeatherResponse
import com.example.weatherapp.utils.checkIfCityExists
import com.example.weatherapp.utils.convertTemperatureToF
import com.example.weatherapp.utils.convertWindSpeedToMph
import com.example.weatherapp.utils.fetchWeatherData
import com.example.weatherapp.utils.formatTime
import com.example.weatherapp.utils.getWeatherForFavorites
import com.example.weatherapp.utils.isNetworkConnectionAvailable
import com.example.weatherapp.utils.isTablet
import com.example.weatherapp.utils.loadFavoriteWeatherList
import com.example.weatherapp.utils.loadFavouriteCities
import com.example.weatherapp.utils.loadPreferenceJson
import com.example.weatherapp.utils.loadPreferenceString
import com.example.weatherapp.utils.saveFavoriteWeatherList
import com.example.weatherapp.utils.saveFavouriteCities
import com.example.weatherapp.utils.savePreferenceJson
import com.example.weatherapp.utils.saveWeatherData
import com.example.weatherapp.utils.searchCitiesByName

class MainActivity : ComponentActivity() {
    @SuppressLint("UnusedMaterial3ScaffoldPaddingParameter")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherAppTheme(darkTheme = true) {
                val context = LocalContext.current
                val selectedScreen = remember { mutableIntStateOf(0) }

                val tablet = remember { isTablet(context) }

                if (tablet) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBarTablet(selectedScreen) }) { innerPadding ->
                        when (selectedScreen.intValue) {
                            0 -> WeatherScreen(modifier = Modifier.padding(innerPadding),tablet = true)
                            1 -> SettingsScreen(modifier = Modifier.padding(innerPadding))
                        }
                    }
                } else {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        bottomBar = { BottomNavigationBar(selectedScreen) }) { innerPadding ->
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










