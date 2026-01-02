package org.veri.be.support.assertion

import org.assertj.core.api.Assertions.assertThat
import org.veri.be.domain.comment.entity.Comment

class CommentAssert private constructor(
    private val actual: Comment
) {
    companion object {
        fun assertThat(actual: Comment): CommentAssert {
            return CommentAssert(actual)
        }
    }

    fun hasId(expected: Long): CommentAssert {
        assertThat(actual.id).isEqualTo(expected)
        return this
    }

    fun hasContent(expected: String): CommentAssert {
        assertThat(actual.content).isEqualTo(expected)
        return this
    }

    fun isDeleted(expected: Boolean): CommentAssert {
        assertThat(actual.isDeleted).isEqualTo(expected)
        return this
    }

    fun hasAuthorId(expected: Long): CommentAssert {
        assertThat(actual.author.id).isEqualTo(expected)
        return this
    }

    fun hasParentId(expected: Long?): CommentAssert {
        assertThat(actual.parent?.id).isEqualTo(expected)
        return this
    }
}
