package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.api.common.dto.MemberProfileResponse;

class ReadingDetailResponseTest {

    @Nested
    @DisplayName("builder")
    class Builder {

        @Test
        @DisplayName("필드가 올바르게 세팅된다")
        void buildsResponse() {
            ReadingDetailResponse.CardSummaryResponse cardSummary =
                    new ReadingDetailResponse.CardSummaryResponse(1L, "https://example.com/1.png", true);
            MemberProfileResponse member = new MemberProfileResponse(1L, "member", "https://example.com/profile.png");

            ReadingDetailResponse response = ReadingDetailResponse.builder()
                    .memberBookId(10L)
                    .member(member)
                    .title("title")
                    .author("author")
                    .imageUrl("https://example.com/book.png")
                    .status(ReadingStatus.READING)
                    .score(4.5)
                    .cardSummaries(List.of(cardSummary))
                    .isPublic(true)
                    .build();

            assertThat(response.memberBookId()).isEqualTo(10L);
            assertThat(response.member()).isEqualTo(member);
            assertThat(response.status()).isEqualTo(ReadingStatus.READING);
            assertThat(response.cardSummaries()).hasSize(1);
        }
    }
}
