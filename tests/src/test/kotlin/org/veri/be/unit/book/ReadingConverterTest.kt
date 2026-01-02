package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CardFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture

@ExtendWith(MockitoExtension::class)
class ReadingConverterTest {

    @Nested
    @DisplayName("toReadingDetailResponse")
    inner class ToReadingDetailResponse {

        @Test
        @DisplayName("소유자라면 → 모든 카드가 포함된다")
        fun returnsAllCardsForOwner() {
            val owner = member(1L, "owner")
            val reading = reading(owner, cards())

            val response = ReadingDetailResponse.from(reading, CurrentMemberInfo.from(JwtClaimsPayload(owner.id, owner.email, owner.nickname, false)))

            assertThat(response.cardSummaries()).hasSize(2)
            assertThat(response.cardSummaries()).extracting<Long> { it.cardId() }
                .containsExactlyInAnyOrder(1L, 2L)
        }

        @Test
        @DisplayName("비소유자라면 → 공개 카드만 포함된다")
        fun filtersCardsForNonOwner() {
            val owner = member(1L, "owner")
            val viewer = member(2L, "viewer")
            val reading = reading(owner, cards())

            val response = ReadingDetailResponse.from(reading, CurrentMemberInfo.from(JwtClaimsPayload(viewer.id, viewer.email, viewer.nickname, false)))

            assertThat(response.cardSummaries()).hasSize(1)
            assertThat(response.cardSummaries()[0].cardId()).isEqualTo(1L)
        }
    }

    private fun cards(): List<org.veri.be.domain.card.entity.Card> {
        val publicCard = CardFixture.aCard()
            .id(1L)
            .image("https://example.com/1.png")
            .isPublic(true)
            .build()
        val privateCard = CardFixture.aCard()
            .id(2L)
            .image("https://example.com/2.png")
            .isPublic(false)
            .build()
        return listOf(publicCard, privateCard)
    }

    private fun reading(owner: org.veri.be.domain.member.entity.Member, cards: List<org.veri.be.domain.card.entity.Card>): org.veri.be.domain.book.entity.Reading {
        val book = BookFixture.aBook()
            .id(10L)
            .title("book")
            .author("author")
            .image("https://example.com/book.png")
            .build()
        return ReadingFixture.aReading()
            .id(100L)
            .member(owner)
            .book(book)
            .cards(cards)
            .isPublic(true)
            .build()
    }

    private fun member(id: Long, nickname: String): org.veri.be.domain.member.entity.Member {
        return MemberFixture.aMember().id(id).nickname(nickname).build()
    }
}
