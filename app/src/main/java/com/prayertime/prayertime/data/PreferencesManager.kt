package com.prayertime.prayertime.data

import android.content.Context
import android.content.SharedPreferences
import com.prayertime.prayertime.data.model.UserLocation

class PreferencesManager(context: Context) {
    private val prefs: SharedPreferences = context.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )

    var isDarkTheme: Boolean
        get() = prefs.getBoolean(KEY_DARK_THEME, true)
        set(value) = prefs.edit().putBoolean(KEY_DARK_THEME, value).apply()

    var savedLocation: UserLocation
        get() {
            val cityName = prefs.getString(KEY_CITY_NAME, UserLocation.DEFAULT.cityName) ?: UserLocation.DEFAULT.cityName
            val apiCityName = prefs.getString(KEY_API_CITY_NAME, UserLocation.DEFAULT.apiCityName) ?: UserLocation.DEFAULT.apiCityName
            val latitude = prefs.getFloat(KEY_LATITUDE, UserLocation.DEFAULT.latitude.toFloat()).toDouble()
            val longitude = prefs.getFloat(KEY_LONGITUDE, UserLocation.DEFAULT.longitude.toFloat()).toDouble()
            val isAutoDetected = prefs.getBoolean(KEY_IS_AUTO_DETECTED, false)
            return UserLocation(cityName, apiCityName, latitude, longitude, isAutoDetected)
        }
        set(value) {
            prefs.edit().apply {
                putString(KEY_CITY_NAME, value.cityName)
                putString(KEY_API_CITY_NAME, value.apiCityName)
                putFloat(KEY_LATITUDE, value.latitude.toFloat())
                putFloat(KEY_LONGITUDE, value.longitude.toFloat())
                putBoolean(KEY_IS_AUTO_DETECTED, value.isAutoDetected)
                apply()
            }
        }

    val hasLocationSet: Boolean
        get() = prefs.contains(KEY_CITY_NAME)

    companion object {
        private const val PREFS_NAME = "prayer_time_prefs"
        private const val KEY_DARK_THEME = "is_dark_theme"
        private const val KEY_CITY_NAME = "city_name"
        private const val KEY_API_CITY_NAME = "api_city_name"
        private const val KEY_LATITUDE = "latitude"
        private const val KEY_LONGITUDE = "longitude"
        private const val KEY_IS_AUTO_DETECTED = "is_auto_detected"
    }
}
