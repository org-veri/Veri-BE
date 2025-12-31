package org.veri.be.domain.reading.event

import org.veri.be.domain.book.entity.enums.ReadingStatus
import java.time.Instant

/**
 * Reading/Card Domain Events
 *
 * Pure data classes for domain events (Spring-free)
 * Published by Application layer using Spring's ApplicationEventPublisher
 */

/**
 * Reading Created Event
 */
data class ReadingCreatedEvent(
    val readingId: Long,
    val memberId: Long,
    val bookId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Reading Status Changed Event
 */
data class ReadingStatusChangedEvent(
    val readingId: Long,
    val memberId: Long,
    val bookId: Long,
    val oldStatus: ReadingStatus,
    val newStatus: ReadingStatus,
    val occurredAt: Instant = Instant.now()
)

/**
 * Reading Visibility Changed Event
 */
data class ReadingVisibilityChangedEvent(
    val readingId: Long,
    val memberId: Long,
    val isPublic: Boolean,
    val occurredAt: Instant = Instant.now()
)

/**
 * Card Created Event
 */
data class CardCreatedEvent(
    val cardId: Long,
    val readingId: Long,
    val memberId: Long,
    val content: String,
    val imageUrl: String,
    val isPublic: Boolean,
    val occurredAt: Instant = Instant.now()
)

/**
 * Card Updated Event
 */
data class CardUpdatedEvent(
    val cardId: Long,
    val readingId: Long,
    val memberId: Long,
    val content: String,
    val imageUrl: String,
    val occurredAt: Instant = Instant.now()
)

/**
 * Card Deleted Event
 */
data class CardDeletedEvent(
    val cardId: Long,
    val readingId: Long,
    val memberId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Card Visibility Changed Event
 */
data class CardVisibilityChangedEvent(
    val cardId: Long,
    val readingId: Long,
    val memberId: Long,
    val isPublic: Boolean,
    val occurredAt: Instant = Instant.now()
)
