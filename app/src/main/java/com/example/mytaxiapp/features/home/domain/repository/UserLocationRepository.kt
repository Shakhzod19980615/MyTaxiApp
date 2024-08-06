package com.example.mytaxiapp.features.home.domain.repository

import com.example.mytaxiapp.features.home.domain.model.UserLocationModel

interface UserLocationRepository {
    suspend fun getLastLocation(): UserLocationModel
    suspend fun insert(location: UserLocationModel)
}