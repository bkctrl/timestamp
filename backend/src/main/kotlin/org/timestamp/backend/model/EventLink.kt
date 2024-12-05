package org.timestamp.backend.model

import jakarta.persistence.Entity
import jakarta.persistence.Table
import jakarta.persistence.Id
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.ManyToOne
import jakarta.persistence.JoinColumn
import org.timestamp.lib.dto.EventLinkDTO
import java.util.UUID

/**
 * Represents a link to an event. This is a 30 minute link that is generated when sharing an event.
 * The link id is used to join an event.
 */
@Entity
@Table(name = "event_links", schema = "public")
class EventLink (
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    val id: UUID? = null,

    @ManyToOne
    @JoinColumn(name = "event_id", nullable = false)
    val event: Event? = null
): Base()

fun EventLink.toDTO(): EventLinkDTO = EventLinkDTO(this.id!!)