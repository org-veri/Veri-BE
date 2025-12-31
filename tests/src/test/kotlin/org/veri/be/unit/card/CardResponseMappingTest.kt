package org.veri.be.unit.card

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.veri.be.member.dto.MemberProfileResponse
import org.veri.be.book.entity.Book
import org.veri.be.book.entity.Reading
import org.veri.be.card.controller.dto.CardConverter
import org.veri.be.card.controller.dto.request.CardCreateRequest
import org.veri.be.card.controller.dto.response.CardDetailResponse
import org.veri.be.card.controller.dto.response.CardListResponse
import org.veri.be.card.controller.dto.response.CardUpdateResponse
import org.veri.be.card.entity.Card
import org.veri.be.card.repository.dto.CardFeedItem
import org.veri.be.card.repository.dto.CardListItem
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import java.time.LocalDateTime

class CardResponseMappingTest {

    @Nested
    @DisplayName("CardConverter")
    inner class CardConverterMapping {

        @Test
        @DisplayName("카드 상세 응답으로 변환한다")
        fun mapsToDetailResponse() {
            val member = member(1L, "member@test.com", "member")
            val reading = reading()
            val card = Card.builder()
                .id(1L)
                .member(member)
                .reading(reading)
                .content("content")
                .image("https://example.com/card.png")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .isPublic(true)
                .build()

            val response: CardDetailResponse = CardConverter.toCardDetailResponse(card, member)

            assertThat(response.id()).isEqualTo(1L)
            assertThat(response.memberProfileResponse()).isEqualTo(MemberProfileResponse.from(member))
            assertThat(response.book()).isNotNull()
            assertThat(response.mine()).isTrue()
        }

        @Test
        @DisplayName("카드 수정 응답으로 변환한다")
        fun mapsToUpdateResponse() {
            val reading = reading()
            val card = Card.builder()
                .id(1L)
                .reading(reading)
                .content("content")
                .image("https://example.com/card.png")
                .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 2, 0, 0))
                .build()

            val response: CardUpdateResponse = CardConverter.toCardUpdateResponse(card)

            assertThat(response.id()).isEqualTo(1L)
            assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0))
            assertThat(response.book()).isNotNull()
        }
    }

    @Nested
    @DisplayName("CardListResponse")
    inner class CardListResponseMapping {

        @Test
        @DisplayName("피드 페이지를 응답으로 변환한다")
        fun mapsFeedPage() {
            val member = member(1L, "member@test.com", "member")
            val item = CardFeedItem(
                1L,
                member,
                "book",
                "content",
                "https://example.com/card.png",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
            )
            val page = PageImpl(
                listOf(item),
                PageRequest.of(0, 10),
                1
            )

            val response = CardListResponse(page)

            assertThat(response.page()).isEqualTo(1)
            assertThat(response.cards()).hasSize(1)
        }

        @Test
        @DisplayName("내 카드 목록을 응답으로 변환한다")
        fun mapsOwnCards() {
            val member = member(1L, "member@test.com", "member")
            val item = CardListItem(
                1L,
                "book",
                "content",
                "https://example.com/card.png",
                LocalDateTime.of(2024, 1, 1, 0, 0),
                true
            )
            val page = PageImpl(
                listOf(item),
                PageRequest.of(1, 5),
                6
            )

            val response = CardListResponse.ofOwn(page, member)

            assertThat(response.page()).isEqualTo(2)
            assertThat(response.cards()).hasSize(1)
            assertThat(response.cards()[0].member().id()).isEqualTo(1L)
        }
    }

    @Nested
    @DisplayName("CardCreateRequest")
    inner class CardCreateRequestDefaults {

        @Test
        @DisplayName("isPublic이 null이면 false로 기본 설정된다")
        fun defaultsIsPublic() {
            val request = CardCreateRequest("content", "https://example.com/card.png", 1L, null)

            assertThat(request.isPublic()).isFalse()
        }
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

    private fun reading(): Reading {
        val book = Book.builder()
            .id(10L)
            .title("book")
            .author("author")
            .image("https://example.com/book.png")
            .build()
        return Reading.builder()
            .id(20L)
            .book(book)
            .build()
    }
}
