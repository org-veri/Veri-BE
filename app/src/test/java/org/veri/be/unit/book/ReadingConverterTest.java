package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.book.dto.reading.ReadingConverter;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.CurrentMemberAccessor;

@ExtendWith(MockitoExtension.class)
class ReadingConverterTest {

    @Mock
    CurrentMemberAccessor currentMemberAccessor;

    ReadingConverter readingConverter;

    @BeforeEach
    void setUp() {
        readingConverter = new ReadingConverter(currentMemberAccessor);
    }

    @Nested
    @DisplayName("toReadingDetailResponse")
    class ToReadingDetailResponse {

        @Test
        @DisplayName("소유자라면 모든 카드가 포함된다")
        void returnsAllCardsForOwner() {
            Member owner = member(1L, "owner@test.com", "owner");
            Reading reading = reading(owner, cards());

            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.of(owner));

            ReadingDetailResponse response = readingConverter.toReadingDetailResponse(reading);

            assertThat(response.cardSummaries()).hasSize(2);
            assertThat(response.cardSummaries()).extracting(ReadingDetailResponse.CardSummaryResponse::cardId)
                    .containsExactlyInAnyOrder(1L, 2L);
        }

        @Test
        @DisplayName("비소유자라면 공개 카드만 포함된다")
        void filtersCardsForNonOwner() {
            Member owner = member(1L, "owner@test.com", "owner");
            Member viewer = member(2L, "viewer@test.com", "viewer");
            Reading reading = reading(owner, cards());

            given(currentMemberAccessor.getCurrentMember()).willReturn(Optional.of(viewer));

            ReadingDetailResponse response = readingConverter.toReadingDetailResponse(reading);

            assertThat(response.cardSummaries()).hasSize(1);
            assertThat(response.cardSummaries().get(0).cardId()).isEqualTo(1L);
        }
    }

    private List<Card> cards() {
        Card publicCard = Card.builder()
                .id(1L)
                .image("https://example.com/1.png")
                .isPublic(true)
                .build();
        Card privateCard = Card.builder()
                .id(2L)
                .image("https://example.com/2.png")
                .isPublic(false)
                .build();
        return List.of(publicCard, privateCard);
    }

    private Reading reading(Member owner, List<Card> cards) {
        Book book = Book.builder()
                .id(10L)
                .title("book")
                .author("author")
                .image("https://example.com/book.png")
                .build();
        return Reading.builder()
                .id(100L)
                .member(owner)
                .book(book)
                .cards(cards)
                .isPublic(true)
                .build();
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
