package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

@Serializable
data class EventUserDTO(
    val id: String,
    val name: String,
    val email: String,
    val pfp: String,
    val timeEst: Long?,
    val distance: Double?,
    val arrived: Boolean,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val arrivedTime: OffsetDateTime?
)