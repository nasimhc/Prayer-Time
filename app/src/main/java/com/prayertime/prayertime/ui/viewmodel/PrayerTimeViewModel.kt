package com.prayertime.prayertime.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.prayertime.prayertime.data.LocationHelper
import com.prayertime.prayertime.data.PreferencesManager
import com.prayertime.prayertime.data.api.ApiClient
import com.prayertime.prayertime.data.model.AladhanTimings
import com.prayertime.prayertime.data.model.PrayerTimes
import com.prayertime.prayertime.data.model.UserLocation
import kotlinx.coroutines.CancellationException
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
    private var fetchJob: Job? = null

    init {
        loadDataForCurrentLocation()
        startPeriodicUpdate()
    }

    fun loadDataForCurrentLocation() {
        val location = _currentLocation.value
        fetchPrayerTimes(location.latitude, location.longitude)
    }

    fun refresh() {
        viewModelScope.launch {
            _isRefreshing.value = true
            val location = _currentLocation.value
            fetchPrayerTimesSync(location.latitude, location.longitude)
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
        fetchJob?.cancel()
    }

    private fun to12HourTime(time24h: String): String {
        val parts = time24h.trim().split(":")
        val hours = parts[0].toIntOrNull() ?: return time24h
        val minutes = parts.getOrNull(1) ?: "00"
        val suffix = if (hours >= 12) "PM" else "AM"
        val hour12 = when {
            hours % 12 == 0 -> 12
            else -> hours % 12
        }
        return String.format(Locale.US, "%d:%s %s", hour12, minutes, suffix)
    }

    private fun mapHttpExceptionMessage(e: retrofit2.HttpException): String {
        val message = when (e.code()) {
            404 -> "Prayer times endpoint not found"
            403 -> "Access forbidden by the server"
            429 -> "Too many requests, please try again later"
            in 500..599 -> "Server error, please try again later"
            else -> e.message()
        }
        return "$message (HTTP ${e.code()})"
    }

    private fun mapAladhanTimingsToPrayerTimes(timings: AladhanTimings): PrayerTimes {
        return PrayerTimes(
            fajr = to12HourTime(timings.fajr),
            shurooq = to12HourTime(timings.sunrise),
            dhuhr = to12HourTime(timings.dhuhr),
            asr = to12HourTime(timings.asr),
            maghrib = to12HourTime(timings.maghrib),
            isha = to12HourTime(timings.isha),
            date_for = ""
        )
    }

    private fun fetchPrayerTimes(lat: Double, lng: Double) {
        // Cancel any in-flight request so a stale response can never overwrite the
        // result for the most recently requested location.
        fetchJob?.cancel()
        fetchJob = viewModelScope.launch {
            _isLoading.value = true
            try {
                fetchPrayerTimesCore(lat, lng)
            } finally {
                // If this request was superseded, the newer request owns the loading
                // state — don't clear it here.
                if (isActive) _isLoading.value = false
            }
        }
    }

    private suspend fun fetchPrayerTimesSync(lat: Double, lng: Double) {
        fetchJob?.cancel()
        fetchPrayerTimesCore(lat, lng)
    }

    private suspend fun fetchPrayerTimesCore(lat: Double, lng: Double) {
        _error.value = null
        try {
            // school defaults to Hanafi (1) in AladhanApi — Asr shadow ratio 2x
            val response = ApiClient.aladhanApi.getPrayerTimes(
                timestamp = System.currentTimeMillis() / 1000,
                latitude = lat,
                longitude = lng
            )
            if (response.code == 200 && response.status == "OK") {
                _prayerTimes.value = mapAladhanTimingsToPrayerTimes(response.data.timings)
                _sunriseSunset.value = Pair(
                    to12HourTime(response.data.timings.sunrise),
                    to12HourTime(response.data.timings.sunset)
                )
                _prayerTimes.value?.let { updateCurrentPrayer(it) }
            } else {
                _error.value = "Failed to load prayer times (${response.status})"
            }
        } catch (e: CancellationException) {
            throw e
        } catch (e: retrofit2.HttpException) {
            _error.value = mapHttpExceptionMessage(e)
        } catch (e: Exception) {
            _error.value = e.message ?: "An error occurred"
        }
    }
}
