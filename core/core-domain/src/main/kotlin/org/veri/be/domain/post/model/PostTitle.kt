package org.veri.be.domain.post.model

/**
 * Post Title Value Object
 *
 * v2.1: Self-validating title with business rules
 */
@JvmInline
value class PostTitle(val value: String) {
    init {
        require(value.isNotBlank()) { "Title cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Title too long: ${value.length} (max $MAX_LENGTH)" }
    }

    companion object {
        const val MAX_LENGTH = 50

        fun of(value: String): PostTitle {
            return PostTitle(value.trim())
        }
    }
}
