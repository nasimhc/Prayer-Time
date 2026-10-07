package com.prayertime.prayertime.data.model

data class UserLocation(
    val cityName: String,          // Display name
    val apiCityName: String,       // API-compatible name (lowercase, no spaces)
    val latitude: Double,
    val longitude: Double,
    val isAutoDetected: Boolean = false
) {
    companion object {
        // Default location: Dhaka, Bangladesh
        val DEFAULT = UserLocation(
            cityName = "Dhaka",
            apiCityName = "dhaka",
            latitude = 23.8103,
            longitude = 90.4125,
            isAutoDetected = false
        )

        // Predefined cities for manual selection
        // apiCityName should match the muslimsalat API format
        val PRESET_CITIES = listOf(
            UserLocation("Dhaka", "dhaka", 23.8103, 90.4125),
            UserLocation("Chittagong", "chittagong", 22.3569, 91.7832),
            UserLocation("Sylhet", "sylhet", 24.8949, 91.8687),
            UserLocation("Rajshahi", "rajshahi", 24.3745, 88.6042),
            UserLocation("Khulna", "khulna", 22.8456, 89.5403),
            UserLocation("Barishal", "barisal", 22.7010, 90.3535),
            UserLocation("Rangpur", "rangpur", 25.7439, 89.2752),
            UserLocation("Mymensingh", "mymensingh", 24.7471, 90.4203),
            UserLocation("Comilla", "comilla", 23.4607, 91.1809),
            UserLocation("Gazipur", "gazipur", 23.9999, 90.4203),
            UserLocation("Mecca", "mecca", 21.4225, 39.8262),
            UserLocation("Medina", "medina", 24.5247, 39.5692),
            UserLocation("Dubai", "dubai", 25.2048, 55.2708),
            UserLocation("London", "london", 51.5074, -0.1278),
            UserLocation("New York", "new-york", 40.7128, -74.0060),
            UserLocation("Toronto", "toronto", 43.6532, -79.3832),
            UserLocation("Sydney", "sydney", -33.8688, 151.2093),
            UserLocation("Kuala Lumpur", "kuala-lumpur", 3.1390, 101.6869),
            UserLocation("Singapore", "singapore", 1.3521, 103.8198),
            UserLocation("Jakarta", "jakarta", -6.2088, 106.8456)
        )
    }
}
