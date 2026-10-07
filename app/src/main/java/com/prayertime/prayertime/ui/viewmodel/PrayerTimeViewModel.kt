package com.prayertime.prayertime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prayertime.prayertime.BuildConfig
import com.prayertime.prayertime.data.LocationHelper
import com.prayertime.prayertime.data.PreferencesManager
import com.prayertime.prayertime.data.api.ApiClient
import com.prayertime.prayertime.data.model.PrayerTimes
import com.prayertime.prayertime.data.model.UserLocation
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PrayerTimeViewModel(application: Application) : AndroidViewModel(application) {
    private val preferencesManager = PreferencesManager(application)
    private val locationHelper = LocationHelper(application)

    private val _prayerTimes = MutableStateFlow<PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimes?> = _prayerTimes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentPrayer = MutableStateFlow<String?>(null)
    val currentPrayer: StateFlow<String?> = _currentPrayer

    private val _nextPrayer = MutableStateFlow<String?>(null)
    val nextPrayer: StateFlow<String?> = _nextPrayer

    private val _countdownToNextPrayer = MutableStateFlow<String?>(null)
    val countdownToNextPrayer: StateFlow<String?> = _countdownToNextPrayer

    private val _sunriseSunset = MutableStateFlow<Pair<String, String>?>(null)
    val sunriseSunset: StateFlow<Pair<String, String>?> = _sunriseSunset

    private val _currentLocation = MutableStateFlow(preferencesManager.savedLocation)
    val currentLocation: StateFlow<UserLocation> = _currentLocation

    private val _isDetectingLocation = MutableStateFlow(false)
    val isDetectingLocation: StateFlow<Boolean> = _isDetectingLocation

    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing

    private var updateJob: Job? = null

    init {
        loadDataForCurrentLocation()
        startPeriodicUpdate()
    }

    fun loadDataForCurrentLocation() {
        val location = _currentLocation.value
        fetchPrayerTimes(location.apiCityName)
        fetchSunriseSunset(location.latitude, location.longitude)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val location = _currentLocation.value
            fetchPrayerTimesSync(location.apiCityName)
            fetchSunriseSunsetSync(location.latitude, location.longitude)
            _isRefreshing.value = false
        }
    }

    fun setLocation(location: UserLocation) {
        _currentLocation.value = location
        preferencesManager.savedLocation = location
        loadDataForCurrentLocation()
    }

    fun detectCurrentLocation() {
        if (!locationHelper.hasLocationPermission()) {
            _error.value = "Location permission not granted"
            return
        }

        viewModelScope.launch {
            _isDetectingLocation.value = true
            try {
                locationHelper.getCurrentLocation()
                    .catch { e ->
                        _error.value = "Failed to get location: ${e.message}"
                        _isDetectingLocation.value = false
                    }
                    .first()
                    .let { result ->
                        result.lastLocation?.let { location ->
                            val userLocation = locationHelper.locationToUserLocation(location)
                            setLocation(userLocation)
                        }
                    }
            } catch (e: Exception) {
                _error.value = "Failed to detect location: ${e.message}"
            } finally {
                _isDetectingLocation.value = false
            }
        }
    }

    fun hasLocationPermission(): Boolean = locationHelper.hasLocationPermission()

    private fun parseTimeToMinutes(timeStr: String): Int? {
        return try {
            val cleanTime = timeStr.trim().lowercase(Locale.US)
            val isPM = cleanTime.contains("pm")
            val timePart = cleanTime.replace("am", "").replace("pm", "").trim()
            val parts = timePart.split(":")

            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            if (isPM && hours != 12) {
                hours += 12
            } else if (!isPM && hours == 12) {
                hours = 0
            }

            hours * 60 + minutes
        } catch (e: Exception) {
            null
        }
    }

    private fun getCurrentTimeInSeconds(): Int {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return hours * 3600 + minutes * 60 + seconds
    }

    private fun minutesToSeconds(minutes: Int): Int = minutes * 60

    private fun updateCurrentPrayer(prayerTimes: PrayerTimes) {
        val currentSeconds = getCurrentTimeInSeconds()

        val prayers = listOf(
            "fajr" to parseTimeToMinutes(prayerTimes.fajr),
            "dhuhr" to parseTimeToMinutes(prayerTimes.dhuhr),
            "asr" to parseTimeToMinutes(prayerTimes.asr),
            "maghrib" to parseTimeToMinutes(prayerTimes.maghrib),
            "isha" to parseTimeToMinutes(prayerTimes.isha)
        ).mapNotNull { (name, time) -> time?.let { name to minutesToSeconds(it) } }

        if (prayers.isEmpty()) {
            _currentPrayer.value = null
            _nextPrayer.value = null
            _countdownToNextPrayer.value = null
            return
        }

        var currentPrayerName: String? = null
        var nextPrayerName: String? = null
        var nextPrayerTimeSeconds: Int? = null

        for ((name, time) in prayers) {
            if (currentSeconds >= time) {
                currentPrayerName = name
            } else if (nextPrayerName == null) {
                nextPrayerName = name
                nextPrayerTimeSeconds = time
            }
        }

        if (currentPrayerName == null) {
            currentPrayerName = "isha"
            nextPrayerName = prayers.firstOrNull()?.first
            nextPrayerTimeSeconds = prayers.firstOrNull()?.second
        }

        if (nextPrayerName == null) {
            nextPrayerName = prayers.firstOrNull()?.first
            nextPrayerTimeSeconds = prayers.firstOrNull()?.second?.let {
                it + SECONDS_IN_DAY
            }
        }

        _currentPrayer.value = currentPrayerName
        _nextPrayer.value = nextPrayerName

        nextPrayerTimeSeconds?.let { nextTime ->
            val remainingSeconds = if (nextTime > currentSeconds) {
                nextTime - currentSeconds
            } else {
                (nextTime + SECONDS_IN_DAY) - currentSeconds
            }
            _countdownToNextPrayer.value = formatCountdown(remainingSeconds)
        } ?: run {
            _countdownToNextPrayer.value = null
        }
    }

    private fun formatCountdown(totalSeconds: Int): String {
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return when {
            hours > 0 -> String.format(Locale.US, "%dh %02dm %02ds", hours, minutes, seconds)
            minutes > 0 -> String.format(Locale.US, "%dm %02ds", minutes, seconds)
            else -> String.format(Locale.US, "%ds", seconds)
        }
    }

    companion object {
        private const val SECONDS_IN_DAY = 24 * 60 * 60
    }

    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isActive) {
                _prayerTimes.value?.let { updateCurrentPrayer(it) }
                delay(1_000)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }

    private fun fetchPrayerTimes(apiCityName: String) {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = ApiClient.prayerTimeApi.getPrayerTimes(
                    city = apiCityName,
                    apiKey = BuildConfig.RAPID_API_KEY
                )
                _prayerTimes.value = response.items.firstOrNull()
                _prayerTimes.value?.let { updateCurrentPrayer(it) }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private suspend fun fetchPrayerTimesSync(apiCityName: String) {
        try {
            _error.value = null
            val response = ApiClient.prayerTimeApi.getPrayerTimes(
                city = apiCityName,
                apiKey = BuildConfig.RAPID_API_KEY
            )
            _prayerTimes.value = response.items.firstOrNull()
            _prayerTimes.value?.let { updateCurrentPrayer(it) }
        } catch (e: Exception) {
            _error.value = e.message ?: "An error occurred"
        }
    }

    private fun fetchSunriseSunset(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val response = ApiClient.sunriseSunsetApi.getSunriseSunset(lat, lng)
                if (response.status == "OK") {
                    val formattedSunrise = response.results.sunrise.split(" ").let { parts ->
                        val time = parts[0].split(":").take(2).joinToString(":")
                        "$time ${parts[1]}"
                    }
                    val formattedSunset = response.results.sunset.split(" ").let { parts ->
                        val time = parts[0].split(":").take(2).joinToString(":")
                        "$time ${parts[1]}"
                    }
                    _sunriseSunset.value = Pair(formattedSunrise, formattedSunset)
                }
            } catch (e: Exception) {
                // Handle error silently for sunrise/sunset
            }
        }
    }

    private suspend fun fetchSunriseSunsetSync(lat: Double, lng: Double) {
        try {
            val response = ApiClient.sunriseSunsetApi.getSunriseSunset(lat, lng)
            if (response.status == "OK") {
                val formattedSunrise = response.results.sunrise.split(" ").let { parts ->
                    val time = parts[0].split(":").take(2).joinToString(":")
                    "$time ${parts[1]}"
                }
                val formattedSunset = response.results.sunset.split(" ").let { parts ->
                    val time = parts[0].split(":").take(2).joinToString(":")
                    "$time ${parts[1]}"
                }
                _sunriseSunset.value = Pair(formattedSunrise, formattedSunset)
            }
        } catch (e: Exception) {
            // Handle error silently for sunrise/sunset
        }
    }
}
