package org.timestamp.backend

import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import org.timestamp.backend.config.FirebaseUser
import org.timestamp.backend.model.Event
import org.timestamp.backend.model.User
import org.timestamp.backend.service.EventService
import org.timestamp.backend.service.UserService
import org.timestamp.lib.dto.EventDTO
import org.timestamp.lib.dto.UserDTO
import java.net.URI

@RestController
@RequestMapping("/test")
class TestController(
    private val eventService: EventService,
    private val userService: UserService
) {

    @RequestMapping("/hello")
    fun hello(): String {
        return "Hello, World!"
    }

    @PostMapping("/user")
    fun createUser(@RequestBody user: User): ResponseEntity<UserDTO> {
        val u = userService.createUser(user)
        return ResponseEntity.created(URI("/test/user/${u.id}")).body(u)
    }

    @GetMapping("/user/{id}")
    fun getUser(@PathVariable id: String): ResponseEntity<User> {
        val u = userService.getUserById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(u)
    }

    @GetMapping("/user/{id}/events")
    fun getUserEvents(@PathVariable id: String): ResponseEntity<List<Event>> {
        val u = userService.getUserById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(u.userEvents.map { it.event!! })
    }

    @PostMapping("/events")
    fun createEvent(@RequestBody event: Event): ResponseEntity<EventDTO> {
        val e = eventService.createEvent(event.creator, event) ?: return ResponseEntity.badRequest().build()
        return ResponseEntity.created(URI("/test/events/${e.id}")).body(e)
    }

    @GetMapping("/events/{id}")
    fun getEvent(@PathVariable id: Long): ResponseEntity<Event> {
        val e = eventService.getEventById(id) ?: return ResponseEntity.notFound().build()
        return ResponseEntity.ok(e)
    }

    @GetMapping("/events")
    fun getEvents(): ResponseEntity<List<Event>> {
        val events = eventService.getAllEvents()
        return ResponseEntity.ok(events)
    }

    @GetMapping("/token")
    fun verifyToken(@AuthenticationPrincipal principal: FirebaseUser): ResponseEntity<String> {
        return ResponseEntity.ok("Token is valid: ${principal.uid}, ${principal.email}")
    }
}