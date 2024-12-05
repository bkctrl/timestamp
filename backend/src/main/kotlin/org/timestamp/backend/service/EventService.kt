package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.*
import org.timestamp.backend.model.*
import org.timestamp.backend.repository.TimestampEventLinkRepository
import org.timestamp.backend.repository.TimestampEventRepository
import org.timestamp.backend.repository.TimestampUserRepository
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.EventLinkDTO
import org.timestamp.lib.dto.NotificationDTO
import org.timestamp.lib.dto.utcNow
import java.util.*


@Service
class EventService(
    private val db: TimestampEventRepository,
    private val userDb: TimestampUserRepository,
    private val eventLinkDb: TimestampEventLinkRepository,
    private val graphHopperService: GraphHopperService
) {
    fun getAllEvents(): List<Event> = db.findAll()

    /**
     * Get events by user ID. We only return events the user isn't part of yet.
     * We will also filter out events that are more than 30 minutes old.
     * Return an event DTO with many fields obscured
     */
    fun getEventByLinkId(firebaseUser: FirebaseUser, id: UUID): EventDTO {
        val user = userDb.findById(firebaseUser.uid).orElseThrow()

        val threshold = utcNow().minusMinutes(30)
        val link = eventLinkDb.findByIdOrNull(id) ?: throw EventLinkNotFoundException()
        if (link.createdAt!!.isBefore(threshold)) throw EventLinkExpiredException()

        val event = link.event!!

        // Check if the user is already part of the event
        if (event.userEvents.firstOrNull { it.id.userId == user.id } != null) throw BadRequestException()

        return event.toHiddenDTO()
    }

    fun getEventById(id: Long): Event? = db.findByIdOrNull(id)

    fun getEvents(firebaseUser: FirebaseUser): List<EventDTO> = db
        .findAllEventsByUser(firebaseUser.uid)
        .map { it.toDTO()}

    /**
     * Create an event with the given user as the creator.
     */
    fun createEvent(userId: String, event: Event): EventDTO {
        val user = userDb.findByIdOrNull(userId) ?: throw EventNotFoundException()
        event.creator = user.id

        val userEvent = graphHopperService.createUserEvent(user, event)
        event.userEvents.add(userEvent)
        return db.save(event).toDTO()
    }

    fun createEvent(user: User, event: Event): EventDTO {
        return createEvent(user.id, event)
    }

    /**
     * Update an event with the given user. The user must be the creator of the event.
     * We will verify the event info by getting the DB version and modifying that.
     * Returns true if the event was updated, false otherwise.
     */
     fun updateEvent(firebaseUser: FirebaseUser, event: Event): EventDTO {
        val item: Event = db.findByIdOrNull(event.id) ?: throw EventNotFoundException()

        if (item.creator != firebaseUser.uid) throw ForbiddenException()

        item.latitude = event.latitude
        item.longitude = event.longitude
        item.address = event.address
        item.arrival = event.arrival
        item.description = event.description
        item.name = event.name

        return db.save(item).toDTO()
    }

    /**
     * Join an event with the given user. The user must exist in the database.
     */
    fun joinEvent(firebaseUser: FirebaseUser, eventLinkId: UUID): EventDTO {
        val user = userDb.findByIdOrNull(firebaseUser.uid)?: throw UserNotFoundException()
        val link = eventLinkDb.findByIdOrNull(eventLinkId) ?: throw EventLinkNotFoundException()
        val threshold = utcNow().minusMinutes(30)

        if (link.createdAt!!.isBefore(threshold)) throw EventLinkExpiredException()

        val event = link.event!!
        if (user.id in event.userEvents.map { it.id.userId }) throw BadRequestException()

        val userEvent = graphHopperService.createUserEvent(user, event)
        event.userEvents.add(userEvent)

        return db.save(event).toDTO()
    }

    fun getEventLink(firebaseUser: FirebaseUser, id: Long): EventLinkDTO {
        val event = db.findByIdOrNull(id) ?: throw EventLinkNotFoundException()

        // Can only get the link if you are the creator
        if (event.creator != firebaseUser.uid) throw ForbiddenException()

        return eventLinkDb.save(EventLink(event = event)).toDTO()
    }

    /**
     * Delete an event. The user must exist in the database.
     * If the user is the creator of the event, the event will be deleted.
     * If the user is NOT the creator, the user will be removed from the event.
     * Returns true if the event was deleted, false otherwise.
     */
    fun deleteEvent(id: Long, firebaseUser: FirebaseUser): Boolean {
        val item: Event = db.findByIdOrNull(id) ?: return false

        if (item.creator == firebaseUser.uid) {
            db.deleteById(id)
            return true
        }

        val joinRow = item.userEvents.firstOrNull { it.id.userId == firebaseUser.uid } ?: return false
        item.userEvents.remove(joinRow)
        db.save(item)
        return true
    }

    /**
     * For now, we are only showing the closest event upcoming. We can change this later.
     * We will return the event and the distance to the event.
     */
    fun getNotifications(firebaseUser: FirebaseUser): NotificationDTO {
        val event = db.findNextEventByUser(firebaseUser.uid) ?: throw EventNotFoundException()
        val userEvent = event.userEvents.firstOrNull { it.id.userId == firebaseUser.uid }
        userEvent ?: throw InternalServerErrorException()

        return graphHopperService.getNotificationDto(userEvent)
    }
}