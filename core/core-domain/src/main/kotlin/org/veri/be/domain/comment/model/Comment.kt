package org.veri.be.domain.comment.model

import java.time.Clock
import java.time.LocalDateTime

/**
 * Comment Aggregate Root (v2.1)
 *
 * Pure domain model with zero infrastructure dependencies
 * - Immutable data class with copy pattern
 * - ID is nullable before persistence
 * - Clock injection for testability
 *
 * @see org.veri.be.domain.comment.entity.Comment (JPA Entity)
 */
data class Comment(
    val id: CommentId?,  // v2.1: null before persistence, non-null after restore
    val postId: Long,
    val postAuthorId: Long,  // Denormalized for notifications
    val authorId: Long,
    val content: CommentContent,
    val parentCommentId: CommentId?,
    val depth: Int,
    val deleted: Boolean,  // v2.1: renamed from isDeleted to avoid JVM clash
    val createdAt: LocalDateTime,
    val deletedAt: LocalDateTime? = null
) {
    /**
     * Check if this is a root comment (no parent)
     */
    fun isRoot(): Boolean = parentCommentId == null

    /**
     * Check if this comment is deleted (soft delete)
     */
    fun isDeleted(): Boolean = deleted || deletedAt != null

    /**
     * Edit comment content (immutable - returns new instance)
     *
     * Business rules:
     * - Only author can edit
     * - Cannot edit deleted comments
     *
     * @param requesterId ID of member requesting edit
     * @param newContent New content
     * @return New Comment instance with updated content
     * @throws DomainException if not authorized or already deleted
     */
    fun editBy(requesterId: Long, newContent: CommentContent): Comment {
        validateAuthor(requesterId)
        validateNotDeleted()
        return copy(content = newContent)
    }

    /**
     * Delete comment (soft delete - immutable returns new instance)
     *
     * Business rules:
     * - Only author can delete
     * - Cannot delete already deleted comments
     * - Content is replaced with deleted marker
     *
     * @param requesterId ID of member requesting deletion
     * @param clock Clock for timestamp
     * @return New Comment instance marked as deleted
     * @throws DomainException if not authorized or already deleted
     */
    fun deleteBy(requesterId: Long, clock: Clock): Comment {
        validateAuthor(requesterId)
        validateNotDeleted()
        return copy(
            deleted = true,
            content = content.asDeleted(),
            deletedAt = LocalDateTime.now(clock).withSecond(0).withNano(0)
        )
    }

    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) {
            throw DomainException("UNAUTHORIZED", "Only author can modify this comment")
        }
    }

    private fun validateNotDeleted() {
        if (isDeleted()) {
            throw DomainException("ALREADY_DELETED", "Cannot modify deleted comment")
        }
    }

    companion object {
        const val MAX_DEPTH = 1  // Allow replies up to 1 level deep

        /**
         * Create new root comment
         *
         * v2.1: Clock injection for testability
         *
         * @param postId Post ID
         * @param postAuthorId Post author ID (for notifications)
         * @param authorId Comment author ID
         * @param content Comment content
         * @param clock Clock for timestamp
         * @return New Comment instance (id = null)
         */
        fun create(
            postId: Long,
            postAuthorId: Long,
            authorId: Long,
            content: CommentContent,
            clock: Clock
        ): Comment {
            val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
            return Comment(
                id = null,  // v2.1: null before persistence
                postId = postId,
                postAuthorId = postAuthorId,
                authorId = authorId,
                content = content,
                parentCommentId = null,
                depth = 0,
                deleted = false,
                createdAt = now
            )
        }

        /**
         * Create reply to existing comment
         *
         * Business rules:
         * - Max depth is 1 (no nested replies)
         * - Cannot reply to deleted comments
         *
         * @param parent Parent comment
         * @param authorId Reply author ID
         * @param content Reply content
         * @param clock Clock for timestamp
         * @return New Comment instance (id = null)
         * @throws DomainException if max depth exceeded or parent deleted
         */
        fun createReply(
            parent: Comment,
            authorId: Long,
            content: CommentContent,
            clock: Clock
        ): Comment {
            if (parent.depth >= MAX_DEPTH) {
                throw DomainException("MAX_DEPTH", "Max reply depth exceeded (max $MAX_DEPTH)")
            }
            if (parent.isDeleted()) {
                throw DomainException("PARENT_DELETED", "Cannot reply to deleted comment")
            }

            val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
            return Comment(
                id = null,  // v2.1: null before persistence
                postId = parent.postId,
                postAuthorId = parent.postAuthorId,
                authorId = authorId,
                content = content,
                parentCommentId = parent.id,
                depth = parent.depth + 1,
                deleted = false,
                createdAt = now
            )
        }

        /**
         * Restore comment from persistence (JPA Entity)
         *
         * v2.1: All fields are non-null after persistence
         *
         * @param id Comment ID (must be positive)
         * @param postId Post ID
         * @param postAuthorId Post author ID
         * @param authorId Comment author ID
         * @param content Content string
         * @param parentCommentId Parent comment ID (null for root)
         * @param depth Reply depth (0 or 1)
         * @param isDeleted Soft delete flag
         * @param createdAt Creation timestamp
         * @param deletedAt Deletion timestamp (null if not deleted)
         * @return Restored Comment instance
         */
        fun restore(
            id: Long,
            postId: Long,
            postAuthorId: Long,
            authorId: Long,
            content: String,
            parentCommentId: Long?,
            depth: Int,
            isDeleted: Boolean,
            createdAt: LocalDateTime,
            deletedAt: LocalDateTime?
        ): Comment {
            return Comment(
                id = CommentId.of(id),  // v2.1: non-null after persistence
                postId = postId,
                postAuthorId = postAuthorId,
                authorId = authorId,
                content = CommentContent.of(content),
                parentCommentId = if (parentCommentId != null) CommentId.of(parentCommentId) else null,
                depth = depth,
                deleted = isDeleted,
                createdAt = createdAt,
                deletedAt = deletedAt
            )
        }
    }
}

/**
 * Domain Exception for business rule violations
 */
data class DomainException(
    val code: String,
    override val message: String
) : RuntimeException("[$code] $message")
