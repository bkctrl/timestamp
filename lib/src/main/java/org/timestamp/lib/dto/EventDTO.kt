package org.timestamp.lib.dto

import kotlinx.serialization.Serializable
import java.time.OffsetDateTime

/**
 * This will extract the detailed information of an event.
 * Includes:
 *  - Time est. for each user to an event
 */
@Serializable
data class EventDTO(
    val id: Long? = null,
    val creator: String = "",
    val name: String = "",
    val description: String = "",
    val address: String = "",
    val latitude: Double = 0.0,
    val longitude: Double = 0.0,
    @Serializable(with = OffsetDateTimeSerializer::class)
    val arrival: OffsetDateTime = OffsetDateTime.now(),
    val users: List<EventUserDTO> = emptyList()
)