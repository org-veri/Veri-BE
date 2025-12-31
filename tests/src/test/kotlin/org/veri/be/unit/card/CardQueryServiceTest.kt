package org.veri.be.unit.card

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.card.controller.dto.CardConverter
import org.veri.be.card.controller.dto.response.CardDetailResponse
import org.veri.be.card.controller.enums.CardSortType
import org.veri.be.card.entity.Card
import org.veri.be.card.service.CardRepository
import org.veri.be.card.repository.dto.CardFeedItem
import org.veri.be.card.service.CardQueryService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import org.springframework.data.domain.Page
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.Pageable
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CardQueryServiceTest {

    @org.mockito.Mock
    private lateinit var cardRepository: CardRepository

    private lateinit var cardQueryService: CardQueryService

    @org.mockito.Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

    @BeforeEach
    fun setUp() {
        cardQueryService = CardQueryService(cardRepository)
    }

    @Nested
    @DisplayName("getOwnedCards")
    inner class GetOwnedCards {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        fun passesPagingWithSort() {
            given(cardRepository.findAllByMemberId(any(Long::class.java), any(Pageable::class.java)))
                .willReturn(Page.empty())

            cardQueryService.getOwnedCards(1L, 1, 20, CardSortType.NEWEST)

            org.mockito.Mockito.verify(cardRepository).findAllByMemberId(any(Long::class.java), pageableCaptor.capture())
            val pageable = pageableCaptor.value
            assertThat(pageable.pageNumber).isEqualTo(1)
            assertThat(pageable.pageSize).isEqualTo(20)
            assertThat(pageable.sort).isEqualTo(CardSortType.NEWEST.sort)
        }
    }

    @Nested
    @DisplayName("getCardDetail")
    inner class GetCardDetail {

        @Test
        @DisplayName("카드 상세 응답을 반환한다")
        fun returnsDetailResponse() {
            val viewer = member(1L, "member@test.com", "member")
            val card = Card.builder().id(1L).member(viewer).build()
            val response: CardDetailResponse = CardConverter.toCardDetailResponse(card, viewer)

            given(cardRepository.findByIdWithAllAssociations(1L)).willReturn(Optional.of(card))

            val result = cardQueryService.getCardDetail(1L, viewer)

            assertThat(result).isEqualTo(response)
        }

        @Test
        @DisplayName("카드가 없으면 예외가 발생한다")
        fun throwsWhenNotFound() {
            given(cardRepository.findByIdWithAllAssociations(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { cardQueryService.getCardDetail(1L, null) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }
    }

    @Nested
    @DisplayName("getCardById")
    inner class GetCardById {

        @Test
        @DisplayName("카드가 없으면 예외가 발생한다")
        fun throwsWhenNotFound() {
            given(cardRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { cardQueryService.getCardById(1L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }
    }

    @Nested
    @DisplayName("getOwnedCardCount")
    inner class GetOwnedCardCount {

        @Test
        @DisplayName("카드 수를 반환한다")
        fun returnsCount() {
            given(cardRepository.countAllByMemberId(1L)).willReturn(3)

            val count = cardQueryService.getOwnedCardCount(1L)

            assertThat(count).isEqualTo(3)
        }
    }

    @Nested
    @DisplayName("getAllCards")
    inner class GetAllCards {

        @Test
        @DisplayName("공개 카드 목록을 페이징 조회한다")
        fun returnsPublicCards() {
            val page: Page<CardFeedItem> = PageImpl(
                listOf(),
                org.springframework.data.domain.PageRequest.of(0, 10),
                0
            )
            given(cardRepository.findAllPublicItems(any(Pageable::class.java))).willReturn(page)

            val result = cardQueryService.getAllCards(0, 10, CardSortType.OLDEST)

            assertThat(result).isEqualTo(page)
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
}
