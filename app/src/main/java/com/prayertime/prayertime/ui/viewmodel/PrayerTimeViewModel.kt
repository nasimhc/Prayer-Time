package com.prayertime.prayertime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertime.prayertime.BuildConfig
import com.prayertime.prayertime.data.api.ApiClient
import com.prayertime.prayertime.data.model.PrayerTimes
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.util.Calendar
import java.util.Locale

class PrayerTimeViewModel : ViewModel() {
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

    private var updateJob: Job? = null

    init {
        fetchPrayerTimes()
        fetchSunriseSunset()
        startPeriodicUpdate()
    }

    /**
     * Parses time string like "5:30 am" or "12:45 PM" to minutes since midnight
     */
    private fun parseTimeToMinutes(timeStr: String): Int? {
        return try {
            val cleanTime = timeStr.trim().lowercase(Locale.US)
            val isPM = cleanTime.contains("pm")
            val timePart = cleanTime.replace("am", "").replace("pm", "").trim()
            val parts = timePart.split(":")

            var hours = parts[0].toInt()
            val minutes = parts[1].toInt()

            // Convert to 24-hour format
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

    /**
     * Gets current time as seconds since midnight
     */
    private fun getCurrentTimeInSeconds(): Int {
        val calendar = Calendar.getInstance()
        val hours = calendar.get(Calendar.HOUR_OF_DAY)
        val minutes = calendar.get(Calendar.MINUTE)
        val seconds = calendar.get(Calendar.SECOND)
        return hours * 3600 + minutes * 60 + seconds
    }

    /**
     * Converts minutes since midnight to seconds since midnight
     */
    private fun minutesToSeconds(minutes: Int): Int = minutes * 60

    /**
     * Determines the current prayer based on prayer times and current time.
     * Current prayer = the most recent prayer that has started.
     * Also calculates the next prayer and countdown.
     */
    private fun updateCurrentPrayer(prayerTimes: PrayerTimes) {
        val currentSeconds = getCurrentTimeInSeconds()

        // Create list of prayers with their times in order (in seconds)
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

        // Find the current prayer (the most recent one that has passed)
        // and the next prayer (the first one that hasn't started yet)
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

        // If no prayer has passed yet today, we're still in Isha from last night
        // and the next prayer is Fajr
        if (currentPrayerName == null) {
            currentPrayerName = "isha"
            nextPrayerName = prayers.firstOrNull()?.first
            nextPrayerTimeSeconds = prayers.firstOrNull()?.second
        }

        // If all prayers have passed, next prayer is Fajr tomorrow
        if (nextPrayerName == null) {
            nextPrayerName = prayers.firstOrNull()?.first
            nextPrayerTimeSeconds = prayers.firstOrNull()?.second?.let {
                it + SECONDS_IN_DAY // Add 24 hours for tomorrow
            }
        }

        _currentPrayer.value = currentPrayerName
        _nextPrayer.value = nextPrayerName

        // Calculate countdown
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

    /**
     * Formats seconds into a readable countdown string (e.g., "2h 35m 10s")
     */
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

    /**
     * Starts a periodic job to update the current prayer and countdown every second
     */
    private fun startPeriodicUpdate() {
        updateJob?.cancel()
        updateJob = viewModelScope.launch {
            while (isActive) {
                _prayerTimes.value?.let { updateCurrentPrayer(it) }
                delay(1_000) // Update every second for smooth countdown
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        updateJob?.cancel()
    }

    private fun fetchPrayerTimes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = ApiClient.prayerTimeApi.getPrayerTimes(BuildConfig.RAPID_API_KEY)
                _prayerTimes.value = response.items.firstOrNull()
                _prayerTimes.value?.let { updateCurrentPrayer(it) }
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }

    private fun fetchSunriseSunset() {
        viewModelScope.launch {
            try {
                val response = ApiClient.sunriseSunsetApi.getSunriseSunset()
                if (response.status == "OK") {
                    // Format times to show hours, minutes and AM/PM
                    val formattedSunrise = response.results.sunrise.split(" ").let { parts ->
                        val time = parts[0].split(":").take(2).joinToString(":")
                        "$time ${parts[1]}" // Adds back AM/PM
                    }
                    val formattedSunset = response.results.sunset.split(" ").let { parts ->
                        val time = parts[0].split(":").take(2).joinToString(":")
                        "$time ${parts[1]}" // Adds back AM/PM
                    }
                    _sunriseSunset.value = Pair(formattedSunrise, formattedSunset)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 