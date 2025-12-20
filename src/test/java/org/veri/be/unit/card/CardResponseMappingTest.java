package org.veri.be.unit.card;

import static org.assertj.core.api.Assertions.assertThat;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.card.controller.dto.CardConverter;
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardListResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;

class CardResponseMappingTest {

    @Nested
    @DisplayName("CardConverter")
    class CardConverterMapping {

        @Test
        @DisplayName("카드 상세 응답으로 변환한다")
        void mapsToDetailResponse() {
            Member member = member(1L, "member@test.com", "member");
            Reading reading = reading();
            Card card = Card.builder()
                    .id(1L)
                    .member(member)
                    .reading(reading)
                    .content("content")
                    .image("https://example.com/card.png")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                    .isPublic(true)
                    .build();

            CardDetailResponse response = CardConverter.toCardDetailResponse(card, member);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.memberProfileResponse()).isEqualTo(MemberProfileResponse.from(member));
            assertThat(response.book()).isNotNull();
            assertThat(response.mine()).isTrue();
        }

        @Test
        @DisplayName("카드 수정 응답으로 변환한다")
        void mapsToUpdateResponse() {
            Reading reading = reading();
            Card card = Card.builder()
                    .id(1L)
                    .reading(reading)
                    .content("content")
                    .image("https://example.com/card.png")
                    .createdAt(LocalDateTime.of(2024, 1, 1, 0, 0))
                    .updatedAt(LocalDateTime.of(2024, 1, 2, 0, 0))
                    .build();

            CardUpdateResponse response = CardConverter.toCardUpdateResponse(card);

            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.updatedAt()).isEqualTo(LocalDateTime.of(2024, 1, 2, 0, 0));
            assertThat(response.book()).isNotNull();
        }

        @Test
        @DisplayName("인스턴스화를 방지한다")
        void canNotInstantiate() {
            assertThatThrownBy(() -> {
                java.lang.reflect.Constructor<CardConverter> constructor = CardConverter.class.getDeclaredConstructor();
                constructor.setAccessible(true);
                constructor.newInstance();
            }).hasRootCauseInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("CardListResponse")
    class CardListResponseMapping {

        @Test
        @DisplayName("피드 페이지를 응답으로 변환한다")
        void mapsFeedPage() {
            Member member = member(1L, "member@test.com", "member");
            CardFeedItem item = new CardFeedItem(
                    1L,
                    member,
                    "book",
                    "content",
                    "https://example.com/card.png",
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    true
            );
            PageImpl<CardFeedItem> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(0, 10),
                    1
            );

            CardListResponse response = new CardListResponse(page);

            assertThat(response.page()).isEqualTo(1);
            assertThat(response.cards()).hasSize(1);
        }

        @Test
        @DisplayName("내 카드 목록을 응답으로 변환한다")
        void mapsOwnCards() {
            Member member = member(1L, "member@test.com", "member");
            CardListItem item = new CardListItem(
                    1L,
                    "book",
                    "content",
                    "https://example.com/card.png",
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    true
            );
            PageImpl<CardListItem> page = new PageImpl<>(
                    List.of(item),
                    PageRequest.of(1, 5),
                    6
            );

            CardListResponse response = CardListResponse.ofOwn(page, member);

            assertThat(response.page()).isEqualTo(2);
            assertThat(response.cards()).hasSize(1);
            assertThat(response.cards().get(0).member().id()).isEqualTo(1L);
        }
    }

    @Nested
    @DisplayName("CardCreateRequest")
    class CardCreateRequestDefaults {

        @Test
        @DisplayName("isPublic이 null이면 false로 기본 설정된다")
        void defaultsIsPublic() {
            CardCreateRequest request = new CardCreateRequest("content", "https://example.com/card.png", 1L, null);

            assertThat(request.isPublic()).isFalse();
        }
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

    private Reading reading() {
        Book book = Book.builder()
                .id(10L)
                .title("book")
                .author("author")
                .image("https://example.com/book.png")
                .build();
        return Reading.builder()
                .id(20L)
                .book(book)
                .build();
    }
}
