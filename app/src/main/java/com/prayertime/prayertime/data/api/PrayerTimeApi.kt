package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.PrayerTimeResponse
import retrofit2.http.GET
import retrofit2.http.Headers

interface PrayerTimeApi {
    @Headers(
        "X-RapidAPI-Key: 50d0293c24msha6a0503a2210e2ap1ce865jsn2dfcb6bbd620",
        "X-RapidAPI-Host: muslimsalat.p.rapidapi.com"
    )
    @GET("dhaka.json")
    suspend fun getPrayerTimes(): PrayerTimeResponse
} 