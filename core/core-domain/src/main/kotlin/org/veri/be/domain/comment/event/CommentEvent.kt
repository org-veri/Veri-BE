package org.veri.be.domain.comment.event

import java.time.Instant

/**
 * Comment Domain Events
 *
 * Pure data classes for domain events (Spring-free)
 * Published by Application layer using Spring's ApplicationEventPublisher
 */

/**
 * Comment Posted Event
 */
data class CommentPostedEvent(
    val commentId: Long,
    val postId: Long,
    val postAuthorId: Long,
    val authorId: Long,
    val parentCommentId: Long?,
    val content: String,
    val occurredAt: Instant = Instant.now()
)

/**
 * Comment Edited Event
 */
data class CommentEditedEvent(
    val commentId: Long,
    val postId: Long,
    val authorId: Long,
    val content: String,
    val occurredAt: Instant = Instant.now()
)

/**
 * Comment Deleted Event
 */
data class CommentDeletedEvent(
    val commentId: Long,
    val postId: Long,
    val authorId: Long,
    val occurredAt: Instant = Instant.now()
)
