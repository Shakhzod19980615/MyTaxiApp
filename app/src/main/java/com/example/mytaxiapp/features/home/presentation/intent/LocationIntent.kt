package com.example.mytaxiapp.features.home.presentation.intent

import com.example.mytaxiapp.features.home.domain.model.UserLocationModel

sealed class LocationIntent {
    object FetchLastLocation : LocationIntent()
}
data class LocationState(
    val isLoading: Boolean = false,
    val location: UserLocationModel? = null,
    val error: String? = null
)
data class LocationViewState(
    val currentLocation: UserLocationModel? = null
)