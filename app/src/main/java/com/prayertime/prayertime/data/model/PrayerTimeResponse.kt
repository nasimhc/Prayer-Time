package com.prayertime.prayertime.data.model

data class PrayerTimeResponse(
    val title: String,
    val query: String,
    val items: List<PrayerTimes>
)

data class PrayerTimes(
    val fajr: String,
    val shurooq: String,
    val dhuhr: String,
    val asr: String,
    val maghrib: String,
    val isha: String,
    val date_for: String
) 