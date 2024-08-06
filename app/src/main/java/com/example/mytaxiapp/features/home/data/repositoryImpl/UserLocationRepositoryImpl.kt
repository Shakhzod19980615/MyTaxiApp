package com.example.mytaxiapp.features.home.data.repositoryImpl

import com.example.mytaxiapp.features.home.data.database.UserLocationDao
import com.example.mytaxiapp.features.home.data.dto.toUserLocationEntity
import com.example.mytaxiapp.features.home.data.dto.toUserLocationModel
import com.example.mytaxiapp.features.home.domain.model.UserLocationModel
import com.example.mytaxiapp.features.home.domain.repository.UserLocationRepository
import javax.inject.Inject

class UserLocationRepositoryImpl @Inject constructor(
    private val userLocationDao: UserLocationDao
):UserLocationRepository {
    override suspend fun getLastLocation(): UserLocationModel {
        return userLocationDao.getLastLocation()?.toUserLocationModel() ?: UserLocationModel(0.0,0.0)
    }

    override suspend fun insert(location: UserLocationModel) {
        return userLocationDao.insert(location.toUserLocationEntity())
    }
}