package org.timestamp.lib.dto

import kotlinx.serialization.Serializable

@Serializable
data class NotificationDTO(
    val event: EventDTO,

    /**
     * The route information for the user to get to the event.
     * Contains all three travel modes.
     */
    val routeInfos : List<RouteInfoDTO>
)