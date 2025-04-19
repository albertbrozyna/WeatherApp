package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import androidx.core.content.edit
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

//function to save preferences
fun savePreference(context: Context, key: String, city: String) {
    val appName = context.getString(R.string.app_name)

    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    sharedPreferences.edit {
        putString(key, city)
    }
}

//Function to load preferences
fun loadPreference(context: Context, key: String): String? {
    val appName = context.getString(R.string.app_name)
    val sharedPreferences: SharedPreferences =
        context.getSharedPreferences(appName, Context.MODE_PRIVATE)

    return sharedPreferences.getString(key, null)
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

//Saving forecast data to file
fun saveWeatherForecastData(
    context: Context, weatherForecast: WeatherForecastList, filename: String
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

suspend fun fetchWeatherData(city: String, apiKey: String): WeatherResponse? {
    try {
        val weatherAPI = WeatherApiClient.weatherAPI
        val response = weatherAPI.getWeatherByCity(city, apiKey)

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

fun saveWeatherData(context: Context, weatherResponse: WeatherResponse, filename: String) {
    try {
        val file = File(context.filesDir, filename)

        val fileOutputStream = FileOutputStream(file)

        val objectOutputStream = ObjectOutputStream(fileOutputStream)

        objectOutputStream.writeObject(weatherResponse)
        objectOutputStream.close()

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadWeatherData(context: Context, filename: String): WeatherResponse? {

    try {
        val file = File(context.filesDir, filename)

        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val weatherResponse = objectInputStream.readObject() as WeatherResponse
            objectInputStream.close()
            return weatherResponse

        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
}

fun saveFavouriteCities(context: Context, cities: List<String>) {
    try {
        val filename = context.getString(R.string.favorite_cities)
        val file = File(context.filesDir, filename)
        file.writeText(cities.joinToString("\n"))

    } catch (e: Exception) {
        e.printStackTrace()
    }
}

fun loadFavouriteCities(context: Context): List<String> {
    try {
        val filename = context.getString(R.string.favorite_cities)

        val file = File(context.filesDir, filename)
        if (file.exists()) {
            return file.readLines()
        }

    } catch (e: Exception) {
        e.printStackTrace()
    }
    return emptyList()
}


//Getting weather for favorite list
suspend fun getWeatherForFavorites(favCities: List<String>, apiKey: String): List<WeatherResponse> {
    val weatherList = mutableListOf<WeatherResponse>()

    for (city in favCities) {
        val response = fetchWeatherData(city, apiKey)
        if (response != null) {
            weatherList.add(response)
        }
    }

    return weatherList
}

//To save weather
fun saveFavoriteWeatherList(
    context: Context, weatherList: List<WeatherResponse>, filename: String
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
fun loadFavoriteWeatherList(context: Context, filename: String): List<WeatherResponse>? {
    try {
        val file = File(context.filesDir, filename)
        if (file.exists()) {
            val fileInputStream = FileInputStream(file)
            val objectInputStream = ObjectInputStream(fileInputStream)
            val list = objectInputStream.readObject() as List<WeatherResponse>
            objectInputStream.close()
            return list
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return null
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
    favCities: List<String>, apiKey: String
): List<WeatherForecastList> {
    val weatherList = mutableListOf<WeatherForecastList>()

    for (city in favCities) {
        val response = fetchWeatherForecast(city, apiKey)
        if (response != null) {
            weatherList.add(response)
        }
    }

    return weatherList
}

fun isTablet(context: Context): Boolean {
    return context.resources.configuration.smallestScreenWidthDp >= 600
}