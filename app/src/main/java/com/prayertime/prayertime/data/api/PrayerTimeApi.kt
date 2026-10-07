package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.PrayerTimeResponse
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface PrayerTimeApi {
    @GET("{city}.json")
    suspend fun getPrayerTimes(
        @Path("city") city: String,
        @Header("X-RapidAPI-Key") apiKey: String,
        @Header("X-RapidAPI-Host") host: String = "muslimsalat.p.rapidapi.com"
    ): PrayerTimeResponse
}
