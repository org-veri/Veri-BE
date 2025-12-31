package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.book.dto.reading.ReadingConverter
import org.veri.be.book.dto.reading.response.ReadingDetailResponse
import org.veri.be.book.entity.Book
import org.veri.be.book.entity.Reading
import org.veri.be.book.service.ReadingCardSummary
import org.veri.be.book.service.ReadingCardSummaryProvider
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.auth.context.CurrentMemberAccessor
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class ReadingConverterTest {

    @org.mockito.Mock
    private lateinit var currentMemberAccessor: CurrentMemberAccessor

    @org.mockito.Mock
    private lateinit var readingCardSummaryProvider: ReadingCardSummaryProvider

    private lateinit var readingConverter: ReadingConverter

    @BeforeEach
    fun setUp() {
        readingConverter = ReadingConverter(currentMemberAccessor, readingCardSummaryProvider)
    }

    @Nested
    @DisplayName("toReadingDetailResponse")
    inner class ToReadingDetailResponse {

        @Test
        @DisplayName("소유자라면 모든 카드가 포함된다")
        fun returnsAllCardsForOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val reading = reading(owner)
            given(readingCardSummaryProvider.getCardSummaries(100L))
                .willReturn(cardSummaries())

            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.of(owner))

            val response = readingConverter.toReadingDetailResponse(reading)

            assertThat(response.cardSummaries()).hasSize(2)
            assertThat(response.cardSummaries()).extracting<Long> { it.cardId() }
                .containsExactlyInAnyOrder(1L, 2L)
        }

        @Test
        @DisplayName("비소유자라면 공개 카드만 포함된다")
        fun filtersCardsForNonOwner() {
            val owner = member(1L, "owner@test.com", "owner")
            val viewer = member(2L, "viewer@test.com", "viewer")
            val reading = reading(owner)
            given(readingCardSummaryProvider.getCardSummaries(100L))
                .willReturn(cardSummaries())

            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.of(viewer))

            val response = readingConverter.toReadingDetailResponse(reading)

            assertThat(response.cardSummaries()).hasSize(1)
            assertThat(response.cardSummaries()[0].cardId()).isEqualTo(1L)
        }
    }

    private fun cardSummaries(): List<ReadingCardSummary> {
        return listOf(
            ReadingCardSummary(1L, "https://example.com/1.png", true),
            ReadingCardSummary(2L, "https://example.com/2.png", false)
        )
    }

    private fun reading(owner: Member): Reading {
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
