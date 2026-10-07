package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.SunriseSunsetResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SunriseSunsetApi {
    @GET("json")
    suspend fun getSunriseSunset(
        @Query("lat") lat: Double,
        @Query("lng") lng: Double
    ): SunriseSunsetResponse
}
