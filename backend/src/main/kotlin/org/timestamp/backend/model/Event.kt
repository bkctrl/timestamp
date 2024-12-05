package org.timestamp.backend.model

import com.fasterxml.jackson.annotation.JsonManagedReference
import jakarta.persistence.*
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventUserDTO
import java.time.OffsetDateTime

@Entity
@Table(name = "events", schema = "public")
class Event(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    // Auto incremented id
    val id: Long? = null,
    var creator: String = "",
    var name: String = "",
    var description: String = "",
    var address: String = "",

    // The location of the event
    var latitude: Double = 0.0,
    var longitude: Double = 0.0,

    // When the event starts
    var arrival: OffsetDateTime = OffsetDateTime.now(),

    @OneToMany(mappedBy = "event", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.LAZY)
    @JsonManagedReference("event-userEvents")
    val userEvents: MutableSet<UserEvent> = mutableSetOf(),
): Base()

fun Event.toDTO(): EventDTO {
    val users: List<EventUserDTO> = this.userEvents.map {
        val user = it.user!!

        // Only get the time est. if the event is today and the user has not arrived
        EventUserDTO(
            id = user.id,
            name = user.name,
            email = user.email,
            pfp = user.pfp,
            timeEst = it.timeEst,
            distance = it.distance,
            arrivedTime = it.arrivedTime,
            arrived = it.arrived
        )
    }

    return EventDTO(
        id = this.id!!,
        creator = this.creator,
        name = this.name,
        description = this.description,
        address = this.address,
        latitude = this.latitude,
        longitude = this.longitude,
        arrival = this.arrival,
        users = users,
    )
}

/**
 * Return an EventDTO with many fields hidden. The owner of the event will
 * be the only user in the list. Location information will be hidden.
 */
fun Event.toHiddenDTO(): EventDTO {
    // We will add only the owner into the user list.
    val ownerUser = this.userEvents.single { this.creator == it.user!!.id }.user!!
    val users: List<EventUserDTO> = listOf(
        EventUserDTO(
            id = ownerUser.id,
            name = ownerUser.name,
            email = ownerUser.email,
            pfp = ownerUser.pfp,
            timeEst = null,
            distance = null,
            arrived = false,
            arrivedTime = null
        )
    )

    return EventDTO(
        name = this.name,
        description = this.description,
        latitude = this.latitude,
        longitude = this.longitude,
        address = this.address,
        arrival = this.arrival,
        users = users
    )
}