package com.example.weatherapp.utils

import com.google.gson.reflect.TypeToken
import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.weatherapp.R
import com.google.gson.Gson
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.core.content.edit

// function to save preferences
fun savePreferenceJson(context: Context, key: String, city: GeoCity) {
    val gson = Gson()
    val json = gson.toJson(city) // Serialize GeoCity to JSON

    val appName = context.getString(R.string.app_name)
    val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    sharedPreferences.edit { putString(key, json) }
}

// Function to load preferences
fun loadPreferenceJson(context: Context, key: String): GeoCity? {
    val gson = Gson()
    val appName = context.getString(R.string.app_name)
    val sharedPreferences = context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    val json = sharedPreferences.getString(key, null)

    return if (json != null) {
        gson.fromJson(json, GeoCity::class.java)
    } else {
        null
    }
}

// Save preference string
fun savePreferenceString(context: Context, key: String, preference: String) {
    val appName = context.getString(R.string.app_name)

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    sharedPreferences.edit { putString(key, preference) }
}

// Load preference string
fun loadPreferenceString(context: Context, key: String): String? {
    val appName = context.getString(R.string.app_name)

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    return sharedPreferences.getString(key, null)
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
    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

    val parsedDate = dateFormat.parse(date)

    if (parsedDate == null) {
        return "Invalid date"
    }

    val dayFormat = SimpleDateFormat("MM-dd", Locale.ENGLISH)
    return dayFormat.format(parsedDate)
}

fun convertTemperatureToF(temp: Float): String {
    return "${(temp * 9 / 5 + 32).toInt()}°F"
}

//To convertSpeed
fun convertWindSpeedToMph(speed: Float): String {
    val speedInMph = speed * 2.23694
    return "${speedInMph.toInt()} mph"

}

suspend fun fetchWeatherData(lat : Float, lon : Float, apiKey: String): WeatherResponse? {
    try {
        val weatherAPI = WeatherApiClient.weatherAPI
        val response = weatherAPI.getWeatherByCoordinates(lat = lat, lon = lon, apiKey = apiKey)

        return response
    } catch (e: Exception) {
        e.printStackTrace()

        if (e.message?.contains("HTTP 404") == true) {
            return null
        }

        return null
    }

}

fun formatTime(unixTime: Long): String {
    val date = Date(unixTime * 1000)
    val format = SimpleDateFormat("HH:mm\ndd MMM yyyy", Locale.ENGLISH)
    return format.format(date)
}

fun isNetworkConnectionAvailable(context: Context): Boolean {
    val connectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork

    if (network == null) {
        return false
    }

    val capabilities = connectivityManager.getNetworkCapabilities(network)

    if (capabilities == null) {
        return false
    }

    return when {
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        else -> false
    }

}



fun saveFavouriteCities(context: Context, cities: List<GeoCity>) {
    try {
        val filename = context.getString(R.string.favorite_cities)
        val file = File(context.filesDir, filename)

        val lines = cities.map { city ->
            val statePart = city.state?.let { ",$it" } ?: ""
            "${city.name},${city.lat},${city.lon},${city.country}$statePart"
        }

        file.writeText(lines.joinToString("\n"))
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


fun loadFavouriteCities(context: Context): List<GeoCity> {
    return try {
        val filename = context.getString(R.string.favorite_cities)
        val file = File(context.filesDir, filename)

        if (file.exists()) {
            file.readLines().mapNotNull { line ->
                val parts = line.split(",")
                if (parts.size >= 4) {
                    val name = parts[0].trim()
                    val lat = parts[1].toFloatOrNull()
                    val lon = parts[2].toFloatOrNull()
                    val country = parts[3].trim()
                    val state = if (parts.size > 4) parts[4].trim() else null

                    if (lat != null && lon != null) {
                        GeoCity(name, lat, lon, country, state)
                    } else null
                } else null
            }
        } else {
            emptyList()
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

//Getting weather for favorite list
suspend fun getWeatherForFavorites(favCities: List<GeoCity>, apiKey: String): List<WeatherResponse> {
    val weatherList = mutableListOf<WeatherResponse>()

    for (city in favCities) {
        val response = fetchWeatherData(lat = city.lat, lon = city.lon, apiKey)
        if (response != null) {
            weatherList.add(response)
        }
    }

    return weatherList
}

//To save weather

fun saveFavoriteWeatherList(context: Context, weatherList: List<WeatherResponse>, filename: String) {
    try {
        val gson = Gson()
        val json = gson.toJson(weatherList)

        val file = File(context.filesDir, filename)
        file.writeText(json)
    } catch (e: Exception) {
        e.printStackTrace()
    }
}



@Suppress("UNCHECKED_CAST")
fun loadFavoriteWeatherList(context: Context, filename: String): List<WeatherResponse>? {
    return try {
        val file = File(context.filesDir, filename)
        if (!file.exists()) return null

        val json = file.readText()
        val gson = Gson()
        val type = object : TypeToken<List<WeatherResponse>>() {}.type
        gson.fromJson(json, type)
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}



//To save and load forecast

fun saveFavoriteForecastList(
    context: Context, weatherList: List<WeatherForecastList>, filename: String
) {
    try {
        val file = File(context.filesDir, filename)
        val fileOutputStream = FileOutputStream(file)
        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(weatherList)
        objectOutputStream.close()
    } catch (e: Exception) {
        e.printStackTrace()
    }
}


@Suppress("UNCHECKED_CAST")
fun loadFavoriteForecastList(context: Context, filename: String): List<WeatherForecastList>? {
    try {
        val file = File(context.filesDir, filename)
        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val list = objectInputStream.readObject() as List<WeatherForecastList>?
            objectInputStream.close()
            return list
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

//Getting weather for favorite list
suspend fun getWeatherForecastForFavorites(
    favCities: List<GeoCity>, apiKey: String
): List<WeatherForecastList> {
    val weatherList = mutableListOf<WeatherForecastList>()

    for (city in favCities) {
        val response = fetchWeatherForecast(city.lat, city.lon, apiKey)
        if (response != null) {
            weatherList.add(response)
        }
    }

    return weatherList
}

fun isTablet(context: Context): Boolean {
    return context.resources.configuration.smallestScreenWidthDp >= 600
}

// Fetch cities for search
suspend fun searchCitiesByName(context: Context,cityName: String): List<GeoCity> {

    val apiKey = context.getString(R.string.api_key)

    return try {
        WeatherApiClient.geocodingAPI.getCityCoordinates(cityName, 10, apiKey)
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    }
}

// Check if city exists using reverse

suspend fun checkIfCityExists(context: Context, lon: Float, lat: Float): GeoCity? {
    return try {
        val apiKey = context.getString(R.string.api_key)

        val weatherAPI = WeatherApiClient.weatherAPI
        val response = weatherAPI.reverseGeocoding(lat = lat, lon = lon, apiKey = apiKey)

        // If response is not empty, create a object
        if (response.isNotEmpty()) {
            val geo = response[0]
            GeoCity(
                name = geo.name,
                lat = geo.lat,
                lon = geo.lon,
                country = geo.country,
                state = geo.state
            )
        } else {
            null
        }

    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}
