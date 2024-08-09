package com.example.mytaxiapp.features.home.domain.use_case

import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import com.example.mytaxiapp.features.home.domain.repository.UserLocationRepository
import javax.inject.Inject

class UserLocationUseCase @Inject constructor(
    private val userLocationRepository: UserLocationRepository
) {
    fun getLastLocation() = userLocationRepository.getLastLocation()
    suspend fun insert(location: UserLocationModel) = userLocationRepository.insert(location)
}