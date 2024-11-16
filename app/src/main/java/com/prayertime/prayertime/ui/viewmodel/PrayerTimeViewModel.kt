package com.prayertime.prayertime.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.prayertime.prayertime.data.api.ApiClient
import com.prayertime.prayertime.data.model.PrayerTimes
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PrayerTimeViewModel : ViewModel() {
    private val _prayerTimes = MutableStateFlow<PrayerTimes?>(null)
    val prayerTimes: StateFlow<PrayerTimes?> = _prayerTimes

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error

    private val _currentPrayer = MutableStateFlow<String?>(null)
    val currentPrayer: StateFlow<String?> = _currentPrayer

    private val _sunriseSunset = MutableStateFlow<Pair<String, String>?>(null)
    val sunriseSunset: StateFlow<Pair<String, String>?> = _sunriseSunset

    init {
        fetchPrayerTimes()
        fetchSunriseSunset()
    }

    private fun updateCurrentPrayer(prayerTimes: PrayerTimes) {
        viewModelScope.launch {
            val currentTime = System.currentTimeMillis()
            // For now, just setting a default value
            _currentPrayer.value = "fajr" // Set to current prayer based on time
        }
    }

    private fun fetchPrayerTimes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = ApiClient.prayerTimeApi.getPrayerTimes()
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
                    _sunriseSunset.value = Pair(response.results.sunrise, response.results.sunset)
                }
            } catch (e: Exception) {
                // Handle error
            }
        }
    }
} 