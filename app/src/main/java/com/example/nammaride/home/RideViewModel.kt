package com.example.nammaride.home

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.nammaride.network.RetrofitClient
import com.example.nammaride.network.RouteItem
import com.example.nammaride.network.VehicleOption
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class RideViewModel : ViewModel() {
    // UI States
    var showFares by mutableStateOf(false)
    var dropLocationText by mutableStateOf("Drop Location")
    var expandedVehicleType by mutableStateOf<String?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf("")
    var rideState by mutableStateOf("SELECTING") // SELECTING, SEARCHING, BOOKED

    // Map & Network States
    var showRoutePicker by mutableStateOf(false)
    var availableRoutes by mutableStateOf<List<RouteItem>>(emptyList())
    var liveFares by mutableStateOf<List<VehicleOption>>(emptyList())
    var destLat by mutableStateOf(0.0)
    var destLng by mutableStateOf(0.0)

    fun initializeWithRoute(initialRoute: RouteItem?) {
        if (initialRoute != null && liveFares.isEmpty() && !isLoading) {
            fetchFares(initialRoute)
        }
    }

    var isSosEnabled by mutableStateOf(false)
        private set

    fun toggleSos(enabled: Boolean) {
        isSosEnabled = enabled
    }

    fun loadAvailableRoutes() {
        showRoutePicker = true
        if (availableRoutes.isEmpty()) {
            viewModelScope.launch(Dispatchers.IO) {
                try {
                    val routes = RetrofitClient.api.getRoutes()
                    withContext(Dispatchers.Main) { availableRoutes = routes }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) { errorMessage = "Failed to load routes: ${e.message}" }
                }
            }
        }
    }

    fun fetchFares(route: RouteItem) {
        dropLocationText = route.destination_name
        destLat = route.dest_lat
        destLng = route.dest_lng
        showRoutePicker = false
        showFares = true
        isLoading = true
        errorMessage = ""

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val response = RetrofitClient.api.getFares(route.route_id)
                withContext(Dispatchers.Main) {
                    liveFares = response.options
                    isLoading = false
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    errorMessage = "Error: ${e.message}"
                    isLoading = false
                }
            }
        }
    }

    fun confirmBooking() {
        // 1. Instantly show the loading screen
        rideState = "SEARCHING"

        // 2. Launch a background coroutine so the UI doesn't freeze
        viewModelScope.launch {
            // Determine the wait time based on the selected vehicle
            val waitTime = when (expandedVehicleType) {
                "Bike" -> 5000L      // 5 seconds
                "Auto" -> 7000L      // 7 seconds
                "Mini Cab" -> 9000L  // 9 seconds
                "SUV" -> 11000L      // 11 seconds
                else -> 5000L        // Default fallback
            }

            // 3. Pause the app for the calculated time
            delay(waitTime)

            // 4. Finally, trigger the "Driver Assigned" screen!
            rideState = "BOOKED"
        }
    }
}