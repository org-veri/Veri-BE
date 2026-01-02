package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CardFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.ReadingFixture
import org.veri.be.support.steps.CardSteps

class SocialCardIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var cardRepository: CardRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var readingRepository: ReadingRepository

    @Nested
    @DisplayName("GET /api/v1/cards")
    inner class GetCardsFeed {
        @Test
        @DisplayName("전체 공개 카드 feed를 조회하면 → 결과를 반환한다")
        fun getCardsFeedSuccess() {
            createCard(true)

            CardSteps.getCardFeed(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").exists())
        }

        @Test
        @DisplayName("정렬 파라미터 오류면 → 400을 반환한다")
        fun invalidSort() {
            CardSteps.getCardFeed(mockMvc, mapOf("sort" to "INVALID"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("page 초과면 → 빈 목록을 반환한다")
        fun pageOverflow() {
            CardSteps.getCardFeed(mockMvc, mapOf("page" to "1000"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards").isEmpty)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    inner class ModifyVisibility {
        @Test
        @DisplayName("공개 -> 비공개면 → 결과를 반환한다")
        fun toPrivate() {
            val card = createCard(true)

            CardSteps.updateVisibility(mockMvc, card.id, false)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(false))
        }

        @Test
        @DisplayName("비공개 -> 공개면 → 결과를 반환한다")
        fun toPublic() {
            val card = createCard(true)
            card.changeVisibility(getMockMember().id, false)
            cardRepository.save(card)

            CardSteps.updateVisibility(mockMvc, card.id, true)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }

        @Test
        @DisplayName("비공개 독서 카드 공개 시도면 → 403을 반환한다")
        fun publicOnPrivateReading() {
            var book = BookFixture.aBook().title("T").image("I").isbn("ISBN").build()
            book = bookRepository.save(book)
            var reading = ReadingFixture.aReading().member(getMockMember()).book(book).isPublic(false).build()
            reading = readingRepository.save(reading)

            var card = CardFixture.aCard()
                .member(getMockMember())
                .reading(reading)
                .content("C")
                .image("I")
                .isPublic(false)
                .build()
            card = cardRepository.save(card)

            CardSteps.updateVisibility(mockMvc, card.id, true)
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("타인 카드면 → 403을 반환한다")
        fun otherCard() {
            val other = MemberFixture.aMember()
                .email("o")
                .nickname("o")
                .profileImageUrl("p")
                .providerId("p")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)

            var book = BookFixture.aBook().title("T").image("I").isbn("ISBN").build()
            book = bookRepository.save(book)
            var reading = ReadingFixture.aReading().member(other).book(book).isPublic(true).build()
            reading = readingRepository.save(reading)

            var card = CardFixture.aCard()
                .member(other)
                .reading(reading)
                .content("C")
                .image("I")
                .isPublic(true)
                .build()
            card = cardRepository.save(card)

            CardSteps.updateVisibility(mockMvc, card.id, false)
                .andExpect(status().isForbidden)
        }
    }

    private fun createCard(isPublic: Boolean): org.veri.be.domain.card.entity.Card {
        var book = BookFixture.aBook().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        var reading = ReadingFixture.aReading().member(getMockMember()).book(book).isPublic(true).build()
        reading = readingRepository.save(reading)

        val card = CardFixture.aCard()
            .member(getMockMember())
            .reading(reading)
            .content("Content")
            .image("Img")
            .isPublic(isPublic)
            .build()
        return cardRepository.save(card)
    }
}
