package org.veri.be.support.assertion

import org.assertj.core.api.Assertions.assertThat
import org.veri.be.domain.card.entity.Card

class CardAssert private constructor(
    private val actual: Card
) {
    companion object {
        fun assertThat(actual: Card): CardAssert {
            return CardAssert(actual)
        }
    }

    fun hasId(expected: Long): CardAssert {
        assertThat(actual.id).isEqualTo(expected)
        return this
    }

    fun hasContent(expected: String?): CardAssert {
        assertThat(actual.content).isEqualTo(expected)
        return this
    }

    fun hasImage(expected: String): CardAssert {
        assertThat(actual.image).isEqualTo(expected)
        return this
    }

    fun isPublic(expected: Boolean): CardAssert {
        assertThat(actual.isPublic).isEqualTo(expected)
        return this
    }

    fun hasMemberId(expected: Long): CardAssert {
        assertThat(actual.member.id).isEqualTo(expected)
        return this
    }

    fun hasReadingId(expected: Long?): CardAssert {
        assertThat(actual.reading?.id).isEqualTo(expected)
        return this
    }
}
