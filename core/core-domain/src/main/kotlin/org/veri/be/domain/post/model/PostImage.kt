package org.veri.be.domain.post.model

/**
 * Post Image Entity (v2.1)
 *
 * Simple value object for post images
 */
data class PostImage(
    val id: Long?,  // v2.1: null before persistence
    val postId: Long,
    val imageUrl: String,
    val displayOrder: Long
) {
    companion object {
        fun create(postId: Long, imageUrl: String, displayOrder: Long): PostImage {
            return PostImage(
                id = null,
                postId = postId,
                imageUrl = imageUrl,
                displayOrder = displayOrder
            )
        }

        fun restore(
            id: Long,
            postId: Long,
            imageUrl: String,
            displayOrder: Long
        ): PostImage {
            return PostImage(
                id = id,
                postId = postId,
                imageUrl = imageUrl,
                displayOrder = displayOrder
            )
        }
    }
}
