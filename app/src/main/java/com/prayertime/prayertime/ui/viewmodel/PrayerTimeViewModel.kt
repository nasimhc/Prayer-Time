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

    init {
        fetchPrayerTimes()
    }

    private fun fetchPrayerTimes() {
        viewModelScope.launch {
            try {
                _isLoading.value = true
                _error.value = null
                val response = ApiClient.prayerTimeApi.getPrayerTimes()
                _prayerTimes.value = response.items.firstOrNull()
            } catch (e: Exception) {
                _error.value = e.message ?: "An error occurred"
            } finally {
                _isLoading.value = false
            }
        }
    }
} 