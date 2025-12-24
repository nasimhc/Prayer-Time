package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.PrayerTimeResponse
import retrofit2.http.GET
import retrofit2.http.Header

interface PrayerTimeApi {
    @GET("dhaka.json")
    suspend fun getPrayerTimes(
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") host: String = "muslimsalat.p.rapidapi.com"
    ): PrayerTimeResponse
} 