package org.timestamp.backend.config

import org.springframework.http.HttpStatus
import org.springframework.web.server.ResponseStatusException

class UserNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
class EventNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND, "Event not found")
class EventLinkNotFoundException : ResponseStatusException(HttpStatus.NOT_FOUND, "Event link not found")
class EventLinkExpiredException : ResponseStatusException(HttpStatus.GONE, "Event link expired")
class BadRequestException : ResponseStatusException(HttpStatus.BAD_REQUEST, "Bad request")
class InternalServerErrorException : ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error")
class ForbiddenException : ResponseStatusException(HttpStatus.FORBIDDEN, "Forbidden")