package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.timestamp.backend.model.EventLink
import java.time.OffsetDateTime
import java.util.*

interface TimestampEventLinkRepository: JpaRepository<EventLink, UUID> {

    @Query("SELECT e FROM EventLink e WHERE e.event.id = :eventId AND e.createdAt >= :threshold")
    fun findByEventIdLastThirtyMinutes(
        @Param("eventId") eventId: Long,
        @Param("threshold") threshold: OffsetDateTime
    ): EventLink?
}