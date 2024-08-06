package com.example.mytaxiapp.features.home.data.dto

import com.example.mytaxiapp.features.home.data.database.UserLocationEntity
import com.example.mytaxiapp.features.home.domain.model.UserLocationModel

fun UserLocationEntity.toUserLocationModel() = UserLocationModel(
    latitude = latitude,
    longitude = longitude
)
fun UserLocationModel.toUserLocationEntity() = UserLocationEntity(
    latitude = latitude,
    longitude = longitude
)