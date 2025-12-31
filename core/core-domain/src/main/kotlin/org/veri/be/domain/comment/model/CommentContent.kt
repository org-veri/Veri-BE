package org.veri.be.domain.comment.model

/**
 * Comment Content Value Object
 *
 * v2.1: Self-validating content with business rules
 * - Non-blank, max 2000 characters
 * - Auto-trimmed on creation
 */
@JvmInline
value class CommentContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Content too long: ${value.length} (max $MAX_LENGTH)" }
    }

    /**
     * Check if content is deleted (soft delete pattern)
     */
    fun isDeletedContent(): Boolean {
        return value == DELETED_CONTENT_MARKER
    }

    /**
     * Get deleted content marker
     */
    fun asDeleted(): CommentContent {
        return CommentContent.of(DELETED_CONTENT_MARKER)
    }

    companion object {
        const val MAX_LENGTH = 2000
        private const val DELETED_CONTENT_MARKER = "삭제된 댓글입니다"

        /**
         * Create CommentContent with validation
         * @param value Raw content string
         * @return Valid CommentContent instance (trimmed)
         */
        fun of(value: String): CommentContent {
            return CommentContent(value.trim())
        }
    }
}
