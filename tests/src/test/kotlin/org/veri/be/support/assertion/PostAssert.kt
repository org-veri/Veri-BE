package org.veri.be.support.assertion

import org.assertj.core.api.Assertions.assertThat
import org.veri.be.domain.post.entity.Post

class PostAssert private constructor(
    private val actual: Post
) {
    companion object {
        fun assertThat(actual: Post): PostAssert {
            return PostAssert(actual)
        }
    }

    fun hasId(expected: Long): PostAssert {
        assertThat(actual.id).isEqualTo(expected)
        return this
    }

    fun hasTitle(expected: String): PostAssert {
        assertThat(actual.title).isEqualTo(expected)
        return this
    }

    fun hasContent(expected: String): PostAssert {
        assertThat(actual.content).isEqualTo(expected)
        return this
    }

    fun isPublic(expected: Boolean): PostAssert {
        assertThat(actual.isPublic).isEqualTo(expected)
        return this
    }

    fun hasAuthorId(expected: Long): PostAssert {
        assertThat(actual.author.id).isEqualTo(expected)
        return this
    }

    fun hasBookId(expected: Long?): PostAssert {
        assertThat(actual.book?.id).isEqualTo(expected)
        return this
    }
}
