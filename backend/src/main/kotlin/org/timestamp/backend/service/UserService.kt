package org.timestamp.backend.service

import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Service
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.config.UserNotFoundException
import org.timestamp.backend.model.User
import org.timestamp.backend.model.toDTO
import org.timestamp.backend.repository.TimestampUserRepository
import org.timestamp.lib.dto.TravelMode
import org.timestamp.lib.dto.UserDTO
import org.timestamp.lib.dto.utcNow

@Service
class UserService(
    private val db: TimestampUserRepository,
    private val graphHopperService: GraphHopperService
) {
    fun getUserById(id: String): User? = db.findByIdOrNull(id)

    /**
     * Create a user from a FirebaseUser object if it does not exist, otherwise return
     * the existing user.
     */
    fun createUser(principal: FirebaseUser): UserDTO {
        val user = User(principal)
        return createUser(user)
    }

    fun createUser(user: User): UserDTO {
        val existingUser: User? = db.findByIdOrNull(user.id)
        if (existingUser != null) {
            // Update fields in case google changes them.
            existingUser.name = user.name
            existingUser.pfp = user.pfp
            db.save(existingUser)
        }

        return (existingUser ?: db.save(user)).toDTO()
    }

    fun updateLocation(
        firebaseUser: FirebaseUser,
        latitude: Double,
        longitude: Double,
        travelMode: TravelMode
    ): UserDTO {
        val user: User = db.findByIdOrNull(firebaseUser.uid) ?: throw UserNotFoundException()
        user.latitude = latitude
        user.longitude = longitude
        user.travelMode = travelMode

        for (userEvent in user.userEvents) {
            // Only update if the event is within the next -2 hours -> 24 hours
            val twoHoursBefore = utcNow().minusHours(2)
            val nextDay = utcNow().plusDays(1)
            val arrival = userEvent.event!!.arrival
            val withinPeriod = arrival.isAfter(twoHoursBefore) && arrival.isBefore(nextDay)

            if (withinPeriod) graphHopperService.updateUserEvent(userEvent)
        }

        return db.save(user).toDTO()
    }
}