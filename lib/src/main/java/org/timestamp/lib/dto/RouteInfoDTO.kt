package org.timestamp.lib.dto

import kotlinx.serialization.Serializable

/**
 * This class is used to display the distance & travel
 * time between the user and the event.
 */
@Serializable
data class RouteInfoDTO(
    /**
     * The distance between the user and the event in meters.
     */
    val distance: Double?,

    /**
     * The estimated time of arrival in milliseconds.
     */
    val timeEst: Long?,
    val travelMode: TravelMode?
)
