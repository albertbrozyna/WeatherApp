package com.example.weatherapp

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

//function to save preferences
fun savePreference(context : Context,key : String,city : String){
    val appName = context.getString(R.string.app_name)

    val sharedPreferences : SharedPreferences = context.getSharedPreferences(appName,Context.MODE_PRIVATE)

    sharedPreferences.edit() {
        putString(key, city)
    }
}

//Function to load preferences
fun loadPreference(context: Context,key: String) : String?{
    val appName = context.getString(R.string.app_name)
    val sharedPreferences : SharedPreferences = context.getSharedPreferences(appName,Context.MODE_PRIVATE)

    return sharedPreferences.getString(key,null)
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
    context: Context,
    weatherForecast: WeatherForecastList,
    filename: String
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