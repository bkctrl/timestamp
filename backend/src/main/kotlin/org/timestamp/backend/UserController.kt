package org.timestamp.backend

import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.*
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.User
import org.timestamp.backend.model.toDTO
import org.timestamp.backend.service.EventService
import org.timestamp.backend.service.UserService
import org.timestamp.lib.dto.LocationDTO
import org.timestamp.lib.dto.NotificationDTO
import org.timestamp.lib.dto.UserDTO

@RestController
@RequestMapping("/users")
class UserController(
    private val userService: UserService,
    private val eventService: EventService
) {

    /**
     * This endpoint is used to verify the ID token from the client and
     * create the user if they do not exist.
     */
    @RequestMapping("/me", method = [RequestMethod.GET, RequestMethod.POST])
    fun getUserAndCreateIfNotExist(
        @AuthenticationPrincipal firebaseUser: FirebaseUser
    ): ResponseEntity<UserDTO> {
        val user = userService.createUser(firebaseUser)
        return ResponseEntity.ok(user)
    }

    @PatchMapping("/me/location")
    fun updateLocation(
        @AuthenticationPrincipal firebaseUser: FirebaseUser,
        @RequestBody location: LocationDTO
    ): ResponseEntity<UserDTO> {
        val user = userService.updateLocation(
            firebaseUser,
            location.latitude,
            location.longitude,
            location.travelMode
        )
        return ResponseEntity.ok(user)
    }

    /**
     * Get the most recent Events that the user is part of.
     * This is used to show the user their upcoming events.
     */
    @GetMapping("/me/notifications")
    suspend fun getNotifications(
        @AuthenticationPrincipal firebaseUser: FirebaseUser
    ): ResponseEntity<NotificationDTO> {
        val notification = eventService.getNotifications(firebaseUser)
        return ResponseEntity.ok(notification)
    }
}