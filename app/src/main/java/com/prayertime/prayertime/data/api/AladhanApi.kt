package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {
    @GET("v1/timings/{timestamp}")
    suspend fun getPrayerTimes(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 1
    ): AladhanResponse
}
