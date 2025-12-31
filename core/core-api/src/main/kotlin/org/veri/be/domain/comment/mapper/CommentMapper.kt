package org.veri.be.domain.comment.mapper

import org.veri.be.domain.comment.entity.Comment as CommentEntity
import org.veri.be.domain.comment.model.Comment
import org.veri.be.domain.comment.model.CommentContent
import org.veri.be.domain.comment.model.CommentId

/**
 * CommentMapper v2.1
 *
 * Bidirectional mapper between JPA Entity and pure Domain model
 * - Entity: Infrastructure concern (storage:db-core)
 * - Domain: Pure business logic (core:core-domain)
 *
 * Separation of concerns:
 * - Entity has JPA annotations, handles persistence
 * - Domain has business rules, immutable, testable without Spring
 */
object CommentMapper {

    /**
     * Convert Domain → Entity (for persistence)
     *
     * v2.1: ID can be null before persistence
     * Note: createdAt/updatedAt are managed by JPA Auditing
     */
    fun toEntity(domain: Comment): CommentEntity {
        return CommentEntity.builder()
            .id(domain.id?.value)  // null before persistence
            .post(null)  // Will be set by repository
            .author(null)  // Will be set by repository
            .content(domain.content.value)
            .parent(null)  // Will be set by repository if needed
            .deletedAt(domain.deletedAt)
            .build()
    }

    /**
     * Convert Entity → Domain (for queries)
     *
     * v2.1: All fields are non-null after persistence
     */
    fun toDomain(entity: CommentEntity): Comment {
        // Get IDs from lazy-loaded relationships
        val postId = entity.post?.id ?: throw IllegalStateException("Post must be loaded")
        val postAuthorId = entity.post?.author?.id ?: throw IllegalStateException("Post author must be loaded")
        val authorId = entity.author?.id ?: throw IllegalStateException("Author must be loaded")
        val parentCommentId = entity.parent?.id

        return Comment.restore(
            id = entity.id!!,
            postId = postId,
            postAuthorId = postAuthorId,
            authorId = authorId,
            content = entity.content,
            parentCommentId = parentCommentId,
            depth = if (entity.parent == null) 0 else 1,
            isDeleted = entity.deletedAt != null,
            createdAt = entity.createdAt,
            deletedAt = entity.deletedAt
        )
    }

    /**
     * Convert List<Entity> → List<Domain>
     */
    fun toDomains(entities: List<CommentEntity>): List<Comment> {
        return entities.map { toDomain(it) }
    }
}
