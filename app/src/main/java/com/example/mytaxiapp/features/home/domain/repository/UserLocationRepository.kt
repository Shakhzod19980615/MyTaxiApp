package com.example.mytaxiapp.features.home.domain.repository

import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import kotlinx.coroutines.flow.Flow

interface UserLocationRepository {
    fun getLastLocation(): Flow<UserLocationModel>
    suspend fun insert(location: UserLocationModel)
}