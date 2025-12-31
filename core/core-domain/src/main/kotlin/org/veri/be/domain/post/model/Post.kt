package org.veri.be.domain.post.model

/**
 * Post Aggregate Root (v2.1)
 *
 * Pure domain model - Book based posting
 * - Independent of Reading (direct Book reference)
 * - No comments collection (Comment is separate AR)
 * - Immutable with copy pattern
 */
data class Post(
    val id: PostId?,  // v2.1: null before persistence
    val authorId: Long,
    val bookId: Long,  // Direct Book reference (Reading independent)
    val title: PostTitle,
    val content: PostContent,
    val isPublic: Boolean,
    val images: List<PostImage>
) {
    /**
     * Publish post (make public)
     */
    fun publishBy(requesterId: Long): Post {
        validateAuthor(requesterId)
        return copy(isPublic = true)
    }

    /**
     * Unpublish post (make private)
     */
    fun unpublishBy(requesterId: Long): Post {
        validateAuthor(requesterId)
        return copy(isPublic = false)
    }

    /**
     * Add image to post
     */
    fun addImage(image: PostImage): Post {
        val newImages = this.images.toMutableList()
        newImages.add(image)
        return copy(images = newImages)
    }

    private fun validateAuthor(requesterId: Long) {
        if (authorId != requesterId) {
            throw DomainException("UNAUTHORIZED", "Only author can modify this post")
        }
    }

    companion object {
        /**
         * Create new post
         *
         * v2.1: id = null, private by default
         */
        fun create(
            authorId: Long,
            bookId: Long,
            title: String,
            content: String
        ): Post {
            return Post(
                id = null,
                authorId = authorId,
                bookId = bookId,
                title = PostTitle.of(title),
                content = PostContent.of(content),
                isPublic = false,
                images = emptyList()
            )
        }

        /**
         * Restore from persistence
         *
         * v2.1: All fields non-null after persistence
         */
        fun restore(
            id: Long,
            authorId: Long,
            bookId: Long,
            title: String,
            content: String,
            isPublic: Boolean,
            images: List<PostImage>
        ): Post {
            return Post(
                id = PostId.of(id),
                authorId = authorId,
                bookId = bookId,
                title = PostTitle.of(title),
                content = PostContent.of(content),
                isPublic = isPublic,
                images = images
            )
        }
    }
}

/**
 * Domain Exception for business rule violations
 * (Shared across all domains - redefined for each package)
 */
data class DomainException(
    val code: String,
    override val message: String
) : RuntimeException("[$code] $message")
