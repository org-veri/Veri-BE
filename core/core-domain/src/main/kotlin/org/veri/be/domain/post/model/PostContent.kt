package org.veri.be.domain.post.model

/**
 * Post Content Value Object
 *
 * v2.1: Self-validating content with business rules
 */
@JvmInline
value class PostContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Content too long: ${value.length} (max $MAX_LENGTH)" }
    }

    companion object {
        const val MAX_LENGTH = 10000

        fun of(value: String): PostContent {
            return PostContent(value.trim())
        }
    }
}
