package org.veri.be.unit.card;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.veri.be.domain.card.controller.dto.CardConverter;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.enums.CardSortType;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.exception.CardErrorCode;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.veri.be.domain.card.service.CardQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class CardQueryServiceTest {

    @Mock
    CardRepository cardRepository;

    CardConverter cardConverter;

    CardQueryService cardQueryService;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setUp() {
        cardQueryService = new CardQueryService(cardRepository);
    }

    @Nested
    @DisplayName("getOwnedCards")
    class GetOwnedCards {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        void passesPagingWithSort() {
            given(cardRepository.findAllByMemberId(any(Long.class), any(Pageable.class)))
                    .willReturn(Page.empty());

            cardQueryService.getOwnedCards(1L, 1, 20, CardSortType.NEWEST);

            org.mockito.Mockito.verify(cardRepository).findAllByMemberId(any(Long.class), pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(CardSortType.NEWEST.getSort());
        }
    }

    @Nested
    @DisplayName("getCardDetail")
    class GetCardDetail {

        @Test
        @DisplayName("카드 상세 응답을 반환한다")
        void returnsDetailResponse() {
            Member viewer = member(1L, "member@test.com", "member");
            Card card = Card.builder().id(1L).member(viewer).build();
            CardDetailResponse response = CardConverter.toCardDetailResponse(card, viewer);

            given(cardRepository.findByIdWithAllAssociations(1L)).willReturn(Optional.of(card));

            CardDetailResponse result = cardQueryService.getCardDetail(1L, viewer);

            assertThat(result).isEqualTo(response);
        }

        @Test
        @DisplayName("카드가 없으면 예외가 발생한다")
        void throwsWhenNotFound() {
            given(cardRepository.findByIdWithAllAssociations(1L)).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> cardQueryService.getCardDetail(1L, null),
                    CardErrorCode.NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("getCardById")
    class GetCardById {

        @Test
        @DisplayName("카드가 없으면 예외가 발생한다")
        void throwsWhenNotFound() {
            given(cardRepository.findById(1L)).willReturn(Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> cardQueryService.getCardById(1L),
                    CardErrorCode.NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("getOwnedCardCount")
    class GetOwnedCardCount {

        @Test
        @DisplayName("카드 수를 반환한다")
        void returnsCount() {
            given(cardRepository.countAllByMemberId(1L)).willReturn(3);

            int count = cardQueryService.getOwnedCardCount(1L);

            assertThat(count).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("getAllCards")
    class GetAllCards {

        @Test
        @DisplayName("공개 카드 목록을 페이징 조회한다")
        void returnsPublicCards() {
            Page<CardFeedItem> page = new PageImpl<>(
                    java.util.List.of(),
                    org.springframework.data.domain.PageRequest.of(0, 10),
                    0
            );
            given(cardRepository.findAllPublicItems(any(Pageable.class))).willReturn(page);

            Page<CardFeedItem> result = cardQueryService.getAllCards(0, 10, CardSortType.OLDEST);

            assertThat(result).isEqualTo(page);
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
}
