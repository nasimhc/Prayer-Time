package com.prayertime.prayertime.data.model

import com.google.gson.annotations.SerializedName

data class AladhanResponse(
    val code: Int,
    val status: String,
    val data: AladhanData
)

data class AladhanData(
    val timings: AladhanTimings,
    val date: AladhanDate
)

data class AladhanTimings(
    @SerializedName("Fajr") val fajr: String,
    @SerializedName("Sunrise") val sunrise: String,
    @SerializedName("Dhuhr") val dhuhr: String,
    @SerializedName("Asr") val asr: String,
    @SerializedName("Sunset") val sunset: String,
    @SerializedName("Maghrib") val maghrib: String,
    @SerializedName("Isha") val isha: String
)

data class AladhanDate(
    val readable: String,
    val gregorian: GregorianDate
)

data class GregorianDate(
    val date: String,      // DD-MM-YYYY
    val weekday: Weekday,
    val month: Month,
    val year: String
)

data class Weekday(
    val en: String
)

data class Month(
    val en: String
)
