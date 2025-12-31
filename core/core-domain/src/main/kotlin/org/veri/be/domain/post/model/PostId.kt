package org.veri.be.domain.post.model

/**
 * Post ID Value Object
 *
 * v2.1: Type-safe ID wrapper with validation
 */
@JvmInline
value class PostId(val value: Long) {
    init {
        require(value > 0) { "Invalid PostId: $value (must be positive)" }
    }

    companion object {
        fun of(value: Long): PostId {
            return PostId(value)
        }
    }
}
