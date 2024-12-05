package org.timestamp.lib.dto

import kotlinx.serialization.Serializable

@Serializable
data class LocationDTO(
    val latitude: Double,
    val longitude: Double,
    val travelMode: TravelMode
)
