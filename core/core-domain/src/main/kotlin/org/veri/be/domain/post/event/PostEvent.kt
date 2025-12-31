package org.veri.be.domain.post.event

import org.veri.be.domain.post.model.PostId
import java.time.Instant

/**
 * Post Domain Events
 *
 * Pure data classes for domain events (Spring-free)
 * Published by Application layer using Spring's ApplicationEventPublisher
 */

/**
 * Post Created Event
 */
data class PostCreatedEvent(
    val postId: Long,
    val authorId: Long,
    val bookId: Long,
    val title: String,
    val occurredAt: Instant = Instant.now()
)

/**
 * Post Published Event
 */
data class PostPublishedEvent(
    val postId: Long,
    val authorId: Long,
    val bookId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Post Unpublished Event
 */
data class PostUnpublishedEvent(
    val postId: Long,
    val authorId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Post Deleted Event
 */
data class PostDeletedEvent(
    val postId: Long,
    val authorId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Post Liked Event
 */
data class PostLikedEvent(
    val postId: Long,
    val memberId: Long,
    val occurredAt: Instant = Instant.now()
)

/**
 * Post Unliked Event
 */
data class PostUnlikedEvent(
    val postId: Long,
    val memberId: Long,
    val occurredAt: Instant = Instant.now()
)
