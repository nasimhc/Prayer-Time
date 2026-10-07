package com.prayertime.prayertime.data.api

import com.prayertime.prayertime.data.model.AladhanResponse
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface AladhanApi {
    /**
     * Prayer times for a coordinate on a given day.
     *
     * `school` is pinned to [SCHOOL_HANAFI] (Asr shadow ratio 2x) so timings match
     * those published by Bangladesh Islamic Foundation. It is a default argument
     * rather than a caller-supplied value so no call site can request the Jumhur
     * (1x) Asr time.
     */
    @GET("v1/timings/{timestamp}")
    suspend fun getPrayerTimes(
        @Path("timestamp") timestamp: Long,
        @Query("latitude") latitude: Double,
        @Query("longitude") longitude: Double,
        @Query("method") method: Int = 1,
        @Query("school") school: Int = SCHOOL_HANAFI
    ): AladhanResponse

    companion object {
        const val SCHOOL_HANAFI = 1
    }
}
