package org.veri.be.domain.post.mapper

import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.post.entity.Post as PostEntity
import org.veri.be.domain.post.entity.PostImage
import org.veri.be.domain.post.model.Post
import org.veri.be.domain.post.model.PostImage as PostImageModel
import org.veri.be.domain.post.model.PostId
import org.veri.be.domain.post.model.PostContent
import org.veri.be.domain.post.model.PostTitle

/**
 * Post Entity-Domain Mapper (v2.1)
 *
 * Strategy B: Separate + Mapper pattern
 */
object PostMapper {

    /**
     * Convert Domain -> Entity
     */
    fun toEntity(domain: Post): PostEntity {
        return PostEntity.builder()
            .id(domain.id?.value)
            .title(domain.title.value)
            .content(domain.content.value)
            .isPublic(domain.isPublic)
            .author(null)  // Set by repository
            .book(null)    // Set by repository
            .build()
    }

    /**
     * Convert Entity -> Domain
     */
    fun toDomain(entity: PostEntity): Post {
        val images = entity.images.map { toDomainImage(it) }
        return Post.restore(
            id = entity.id!!,
            authorId = entity.author?.id!!,
            bookId = entity.book?.id!!,
            title = entity.title,
            content = entity.content,
            isPublic = entity.isPublic,
            images = images
        )
    }

    /**
     * Convert PostImage Entity -> Domain
     */
    private fun toDomainImage(entity: PostImage): PostImageModel {
        return PostImageModel.restore(
            id = entity.id!!,
            postId = entity.post?.id!!,
            imageUrl = entity.imageUrl,
            displayOrder = entity.displayOrder
        )
    }

    /**
     * Convert PostImage Domain -> Entity
     */
    fun toImageEntity(domain: PostImageModel, postEntity: PostEntity): PostImage {
        return PostImage.builder()
            .id(domain.id)
            .post(postEntity)
            .imageUrl(domain.imageUrl)
            .displayOrder(domain.displayOrder)
            .build()
    }
}
