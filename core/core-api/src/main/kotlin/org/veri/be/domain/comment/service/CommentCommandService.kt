package org.veri.be.domain.comment.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.entity.Comment as CommentEntity
import org.veri.be.domain.comment.event.CommentDeletedEvent
import org.veri.be.domain.comment.event.CommentEditedEvent
import org.veri.be.domain.comment.event.CommentPostedEvent
import org.veri.be.domain.comment.mapper.CommentMapper
import org.veri.be.domain.comment.model.Comment
import org.veri.be.domain.comment.model.CommentContent
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.post.service.PostQueryService
import java.time.Clock

/**
 * Comment Command Service v2.1
 *
 * Uses pure Domain Model for business logic
 * - Domain: Rich business rules, immutable, testable
 * - Entity: Persistence only (infrastructure concern)
 * - Mapper: Separates domain from infrastructure
 * - Events: Published for side effects
 */
@Service
class CommentCommandService(
    private val commentRepository: CommentRepository,
    private val commentQueryService: CommentQueryService,
    private val postQueryService: PostQueryService,
    private val clock: Clock,
    private val eventPublisher: ApplicationEventPublisher
) {

    /**
     * Post new comment (root level)
     *
     * v2.1: Domain model handles business logic
     */
    @Transactional
    fun postComment(request: CommentPostRequest, member: Member): Long {
        // 1. Load Post (for validation)
        val post = postQueryService.getPostById(request.postId)

        // 2. Create Domain Model (v2.1: Clock injected, ID = null)
        val comment = Comment.create(
            postId = post.id!!,
            postAuthorId = post.author!!.id!!,
            authorId = member.id!!,
            content = CommentContent.of(request.content),
            clock = clock
        )

        // 3. Build Entity from Domain (immutable, use builder)
        val entity = CommentEntity.builder()
            .id(comment.id?.value)  // null before persistence
            .content(comment.content.value)
            .deletedAt(comment.deletedAt)
            .post(post)
            .author(member)
            .parent(null)  // Root comment has no parent
            .build()

        // 4. Save and get generated ID
        val saved = commentRepository.save(entity)

        // 5. Publish event
        eventPublisher.publishEvent(
            CommentPostedEvent(
                commentId = saved.id!!,
                postId = post.id!!,
                postAuthorId = post.author!!.id!!,
                authorId = member.id!!,
                parentCommentId = null,
                content = request.content
            )
        )

        // 6. Return ID
        return saved.id
    }

    /**
     * Post reply to existing comment
     *
     * v2.1: Domain model validates business rules
     */
    @Transactional
    fun postReply(parentCommentId: Long, content: String, member: Member): Long {
        // 1. Load parent comment (as domain)
        val parentEntity = commentQueryService.getCommentById(parentCommentId)
        val parent = CommentMapper.toDomain(parentEntity)

        // 2. Create reply using Domain model (validates depth, deleted status)
        val reply = Comment.createReply(
            parent = parent,
            authorId = member.id!!,
            content = CommentContent.of(content),
            clock = clock
        )

        // 3. Build Entity from Domain (immutable, use builder)
        val replyEntity = CommentEntity.builder()
            .id(reply.id?.value)  // null before persistence
            .content(reply.content.value)
            .deletedAt(reply.deletedAt)
            .post(parentEntity.post)
            .author(member)
            .parent(parentEntity)
            .build()

        // 4. Save and return ID
        val saved = commentRepository.save(replyEntity)

        // 5. Publish event
        eventPublisher.publishEvent(
            CommentPostedEvent(
                commentId = saved.id!!,
                postId = parentEntity.post!!.id!!,
                postAuthorId = parent.postAuthorId,
                authorId = member.id!!,
                parentCommentId = parentCommentId,
                content = content
            )
        )

        return saved.id
    }

    /**
     * Edit comment content
     *
     * v2.1: Domain model handles authorization and validation
     */
    fun editComment(commentId: Long, content: String, member: Member) {
        // 1. Load comment (as domain)
        val entity = commentQueryService.getCommentById(commentId)
        val comment = CommentMapper.toDomain(entity)

        // 2. Execute business logic (validates author, deleted status)
        val edited = comment.editBy(member.id!!, CommentContent.of(content))

        // 3. Convert back to Entity and save (rebuild entity with updated values)
        val updatedEntity = CommentEntity.builder()
            .id(entity.id)  // Preserve ID
            .content(edited.content.value)
            .deletedAt(edited.deletedAt)
            .post(entity.post)
            .author(entity.author)
            .parent(entity.parent)
            .createdAt(entity.createdAt)
            .updatedAt(entity.updatedAt)
            .build()

        commentRepository.save(updatedEntity)

        // 4. Publish event
        eventPublisher.publishEvent(
            CommentEditedEvent(
                commentId = commentId,
                postId = entity.post!!.id!!,
                authorId = member.id!!,
                content = content
            )
        )
    }

    /**
     * Delete comment (soft delete)
     *
     * v2.1: Domain model handles authorization and soft delete logic
     */
    @Transactional
    fun deleteComment(commentId: Long, member: Member) {
        // 1. Load comment (as domain)
        val entity = commentQueryService.getCommentById(commentId)
        val comment = CommentMapper.toDomain(entity)

        // 2. Execute business logic (validates author, deleted status)
        val deleted = comment.deleteBy(member.id!!, clock)

        // 3. Convert back to Entity and save (rebuild entity with updated values)
        val updatedEntity = CommentEntity.builder()
            .id(entity.id)  // Preserve ID
            .content(deleted.content.value)
            .deletedAt(deleted.deletedAt)
            .post(entity.post)
            .author(entity.author)
            .parent(entity.parent)
            .createdAt(entity.createdAt)
            .updatedAt(entity.updatedAt)
            .build()

        commentRepository.save(updatedEntity)

        // 4. Publish event
        eventPublisher.publishEvent(
            CommentDeletedEvent(
                commentId = commentId,
                postId = entity.post!!.id!!,
                authorId = member.id!!
            )
        )
    }
}
