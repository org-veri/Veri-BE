package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse
import org.veri.be.domain.book.entity.enums.ReadingStatus

class ReadingDetailResponseTest {

    @Nested
    @DisplayName("builder")
    inner class Builder {

        @Test
        @DisplayName("빌더로 생성하면 → 필드가 세팅된다")
        fun buildsResponse() {
            val cardSummary = ReadingDetailResponse.CardSummaryResponse(
                1L,
                "https://example.com/1.png",
                true
            )
            val member = MemberProfileResponse(
                1L,
                "member",
                "https://example.com/profile.png"
            )

            val response = ReadingDetailResponse.builder()
                .memberBookId(10L)
                .member(member)
                .title("title")
                .author("author")
                .imageUrl("https://example.com/book.png")
                .status(ReadingStatus.READING)
                .score(4.5)
                .cardSummaries(listOf(cardSummary))
                .isPublic(true)
                .build()

            assertThat(response.memberBookId()).isEqualTo(10L)
            assertThat(response.member()).isEqualTo(member)
            assertThat(response.status()).isEqualTo(ReadingStatus.READING)
            assertThat(response.cardSummaries()).hasSize(1)
        }
    }
}
