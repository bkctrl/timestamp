package org.timestamp.backend.repository

import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.data.repository.query.Param
import org.timestamp.backend.model.Event

interface TimestampEventRepository : JpaRepository<Event, Long> {

    /**
     * Filter events by current user, that is >= to Today
     */
    @Query("SELECT e FROM Event e JOIN e.userEvents ue WHERE ue.id.userId = :userId AND e.arrival >= CURRENT_TIMESTAMP - 2 HOUR")
    fun findAllEventsByUser(@Param("userId") userId: String): List<Event>

    @Query("SELECT e FROM Event e JOIN e.userEvents ue WHERE ue.id.userId = :userId AND e.arrival BETWEEN CURRENT_TIMESTAMP AND CURRENT_TIMESTAMP + 24 HOUR ORDER BY e.arrival ASC LIMIT 1")
    fun findNextEventByUser(@Param("userId") userId: String): Event?
}