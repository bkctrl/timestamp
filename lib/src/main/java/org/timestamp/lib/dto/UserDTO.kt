package org.timestamp.lib.dto

import kotlinx.serialization.Serializable

@Serializable
data class UserDTO(
    val id: String,
    val name: String,
    val email: String,
    val pfp: String,

    // Current location of the user
    val latitude: Double,
    val longitude: Double,
)