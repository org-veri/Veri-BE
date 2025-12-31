package org.veri.be.domain.post.service

import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.service.BookService
import org.veri.be.domain.card.entity.CardErrorInfo
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.post.dto.request.PostCreateRequest
import org.veri.be.domain.post.dto.response.LikeInfoResponse
import org.veri.be.domain.post.entity.LikePost
import org.veri.be.domain.post.entity.Post as PostEntity
import org.veri.be.domain.post.entity.PostImage
import org.veri.be.domain.post.event.PostCreatedEvent
import org.veri.be.domain.post.event.PostDeletedEvent
import org.veri.be.domain.post.event.PostLikedEvent
import org.veri.be.domain.post.event.PostPublishedEvent
import org.veri.be.domain.post.event.PostUnlikedEvent
import org.veri.be.domain.post.event.PostUnpublishedEvent
import org.veri.be.domain.post.mapper.PostMapper
import org.veri.be.domain.post.model.Post
import org.veri.be.domain.post.model.PostImage as PostImageModel
import org.veri.be.domain.post.repository.LikePostRepository
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.global.storage.service.StorageConstants
import org.veri.be.global.storage.service.StorageService
import org.veri.be.global.storage.service.StorageUtil
import org.veri.be.lib.exception.ApplicationException

/**
 * Post Command Service (v2.1)
 *
 * Uses Domain Model for business logic
 * Publishes domain events using Spring's ApplicationEventPublisher
 */
@Service
class PostCommandService(
    private val postRepository: PostRepository,
    private val postQueryService: PostQueryService,
    private val bookService: BookService,
    private val storageService: StorageService,
    private val likePostRepository: LikePostRepository,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createPost(request: PostCreateRequest, member: Member): Long {
        val book: Book = this.bookService.getBookById(request.bookId())

        // Use domain model for creation
        val post: Post = Post.create(
            authorId = member.id!!,
            bookId = book.id!!,
            title = request.title(),
            content = request.content()
        )

        // Convert to entity for persistence with builder pattern
        val entity: PostEntity = PostEntity.builder()
            .id(post.id?.value)
            .title(post.title.value)
            .content(post.content.value)
            .isPublic(post.isPublic)
            .author(member)
            .book(book)
            .build()

        // Add images
        request.images().forEachIndexed { index, imageUrl ->
            val imageEntity = PostImage.builder()
                .post(entity)
                .imageUrl(imageUrl)
                .displayOrder((index + 1).toLong())
                .build()
            entity.images.add(imageEntity)
        }

        val saved: PostEntity = this.postRepository.save(entity)

        // Publish event
        eventPublisher.publishEvent(
            PostCreatedEvent(
                postId = saved.id!!,
                authorId = member.id!!,
                bookId = book.id!!,
                title = request.title()
            )
        )

        return saved.id
    }

    @Transactional
    fun deletePost(postId: Long, member: Member) {
        val post: PostEntity = this.postQueryService.getPostById(postId)
        post.authorizeOrThrow(member.id)
        this.postRepository.deleteById(postId)

        // Publish event
        eventPublisher.publishEvent(
            PostDeletedEvent(
                postId = postId,
                authorId = member.id!!
            )
        )
    }

    @Transactional
    fun publishPost(postId: Long, member: Member) {
        val entity: PostEntity = this.postQueryService.getPostById(postId)
        val post: Post = PostMapper.toDomain(entity)
        val published: Post = post.publishBy(member.id)

        // Update entity with domain state
        entity.isPublic = published.isPublic
        postRepository.save(entity)

        // Publish event
        eventPublisher.publishEvent(
            PostPublishedEvent(
                postId = postId,
                authorId = member.id!!,
                bookId = entity.book?.id!!
            )
        )
    }

    @Transactional
    fun unPublishPost(postId: Long, member: Member) {
        val entity: PostEntity = this.postQueryService.getPostById(postId)
        val post: Post = PostMapper.toDomain(entity)
        val unpublished: Post = post.unpublishBy(member.id)

        // Update entity with domain state
        entity.isPublic = unpublished.isPublic
        postRepository.save(entity)

        // Publish event
        eventPublisher.publishEvent(
            PostUnpublishedEvent(
                postId = postId,
                authorId = member.id!!
            )
        )
    }

    fun getPresignedUrl(request: PresignedUrlRequest): PresignedUrlResponse {
        if (request.contentLength() > StorageConstants.MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE)
        }

        if (!StorageUtil.isImage(request.contentType())) {
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE)
        }

        return storageService.generatePresignedUrlOfDefault(
            request.contentType(),
            request.contentLength()
        )
    }

    @Transactional
    fun likePost(postId: Long, member: Member): LikeInfoResponse {
        if (likePostRepository.existsByPostIdAndMemberId(postId, member.id)) {
            return LikeInfoResponse(likePostRepository.countByPostId(postId), true)
        }

        val likePost = LikePost.builder()
            .post(postQueryService.getPostById(postId))
            .member(member)
            .build()

        likePostRepository.save(likePost)

        // Publish event
        eventPublisher.publishEvent(
            PostLikedEvent(
                postId = postId,
                memberId = member.id!!
            )
        )

        return LikeInfoResponse(likePostRepository.countByPostId(postId), true)
    }

    @Transactional
    fun unlikePost(postId: Long, member: Member): LikeInfoResponse {
        likePostRepository.deleteByPostIdAndMemberId(postId, member.id)

        // Publish event
        eventPublisher.publishEvent(
            PostUnlikedEvent(
                postId = postId,
                memberId = member.id!!
            )
        )

        return LikeInfoResponse(likePostRepository.countByPostId(postId), false)
    }
}
