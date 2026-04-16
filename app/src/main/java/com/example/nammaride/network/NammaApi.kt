package com.example.nammaride.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

// --- AUTHENTICATION ---
data class AuthRequest(val firebase_uid: String, val phone_number: String)
data class AuthResponse(val status: String, val message: String, val is_new_user: Boolean)
data class RouteItem(val route_id: Int, val destination_name: String, val dest_lat: Double, val dest_lng: Double)

// --- FARES & ROUTING (Matches get_fares.php exactly!) ---
data class FareResponse(
    val status: String,
    val route_id: Int,
    val options: List<VehicleOption>
)

data class VehicleOption(
    val vehicle_type: String,
    val display_name: String,
    val destination: String,
    val total_fare: Double,
    val surge_active: Boolean,
    val surge_message: String,
    val breakdown_ui: BreakdownUi,
    val driver_details: DriverDetails
)

data class BreakdownUi(
    val base_fare: Double,
    val distance_charge: Double,
    val time_charge: Double,
    val booking_fee: Double,
    val government_tax_5_percent: Double,
    val surge_applied: Double
)

data class DriverDetails(
    val driver_name: String,
    val vehicle_model: String,
    val license_plate: String,
    val rating: String,
    val otp: String
)

interface NammaRideApi {
    @POST("auth.php")
    suspend fun authenticateUser(@Body request: AuthRequest): AuthResponse

    @GET("get_fares.php")
    suspend fun getFares(@Query("route_id") routeId: Int): FareResponse

    @GET("get_routes.php")
    suspend fun getRoutes(): List<RouteItem>
}

object RetrofitClient {
    // ✅ After Railway deployment: Settings → Networking → Generate Domain
    // Replace the URL below with your actual Railway domain
    // Example: "https://nammaride-backend-production.up.railway.app/"
    private const val BASE_URL = "https://nammaride-backend-production.up.railway.app/"

    val api: NammaRideApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(NammaRideApi::class.java)
    }
}