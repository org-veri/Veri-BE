package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.CurrentMemberInfo

@ExtendWith(MockitoExtension::class)
class ReadingConverterTest {

    @Nested
    @DisplayName("toReadingDetailResponse")
    inner class ToReadingDetailResponse {

        @Test
        @DisplayName("소유자라면 모든 카드가 포함된다")
        fun returnsAllCardsForOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = reading(owner, cards())

            val response = ReadingDetailResponse.from(reading, CurrentMemberInfo.from(JwtClaimsPayload(owner.id, owner.email, owner.nickname, false)))

            assertThat(response.cardSummaries()).hasSize(2)
            assertThat(response.cardSummaries()).extracting<Long> { it.cardId() }
                .containsExactlyInAnyOrder(1L, 2L)
        }

        @Test
        @DisplayName("비소유자라면 공개 카드만 포함된다")
        fun filtersCardsForNonOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val viewer = member(2L, "viewer@test.com", "viewer")
            val reading = reading(owner, cards())

            val response = ReadingDetailResponse.from(reading, CurrentMemberInfo.from(JwtClaimsPayload(viewer.id, viewer.email, viewer.nickname, false)))

            assertThat(response.cardSummaries()).hasSize(1)
            assertThat(response.cardSummaries()[0].cardId()).isEqualTo(1L)
        }
    }

    private fun cards(): List<Card> {
        val publicCard = Card.builder()
            .id(1L)
            .image("https://example.com/1.png")
            .isPublic(true)
            .build()
        val privateCard = Card.builder()
            .id(2L)
            .image("https://example.com/2.png")
            .isPublic(false)
            .build()
        return listOf(publicCard, privateCard)
    }

    private fun reading(owner: Member, cards: List<Card>): Reading {
        val book = Book.builder()
            .id(10L)
            .title("book")
            .author("author")
            .image("https://example.com/book.png")
            .build()
        return Reading.builder()
            .id(100L)
            .member(owner)
            .book(book)
            .cards(cards)
            .isPublic(true)
            .build()
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
