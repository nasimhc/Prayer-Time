package com.prayertime.prayertime.data.model

data class SunriseSunsetResponse(
    val results: SunriseSunsetResults,
    val status: String
)

data class SunriseSunsetResults(
    val sunrise: String,
    val sunset: String
) 