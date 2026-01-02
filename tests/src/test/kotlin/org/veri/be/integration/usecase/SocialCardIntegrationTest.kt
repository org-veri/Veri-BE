package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.integration.IntegrationTestSupport

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
        @DisplayName("전체 공개 카드 feed 최신순")
        fun getCardsFeedSuccess() {
            createCard(true)

            mockMvc.perform(get("/api/v1/cards"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards[0].cardId").exists())
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        fun invalidSort() {
            mockMvc.perform(
                get("/api/v1/cards")
                    .param("sort", "INVALID")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("page 초과")
        fun pageOverflow() {
            mockMvc.perform(
                get("/api/v1/cards")
                    .param("page", "1000")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.cards").isEmpty)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    inner class ModifyVisibility {
        @Test
        @DisplayName("공개 -> 비공개")
        fun toPrivate() {
            val card = createCard(true)

            mockMvc.perform(
                patch("/api/v1/cards/${card.id}/visibility")
                    .param("isPublic", "false")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(false))
        }

        @Test
        @DisplayName("비공개 -> 공개")
        fun toPublic() {
            val card = createCard(true)
            card.changeVisibility(getMockMember().id, false)
            cardRepository.save(card)

            mockMvc.perform(
                patch("/api/v1/cards/${card.id}/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }

        @Test
        @DisplayName("비공개 독서에 속한 카드 공개 시도")
        fun publicOnPrivateReading() {
            var book = Book.builder().title("T").image("I").isbn("ISBN").build()
            book = bookRepository.save(book)
            var reading = Reading.builder().member(getMockMember()).book(book).isPublic(false).build()
            reading = readingRepository.save(reading)

            var card = Card.builder()
                .member(getMockMember())
                .reading(reading)
                .content("C")
                .image("I")
                .isPublic(false)
                .build()
            card = cardRepository.save(card)

            mockMvc.perform(
                patch("/api/v1/cards/${card.id}/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isForbidden)
        }

        @Test
        @DisplayName("타인 카드")
        fun otherCard() {
            val other = Member.builder()
                .email("o")
                .nickname("o")
                .profileImageUrl("p")
                .providerId("p")
                .providerType(ProviderType.KAKAO)
                .build()
            memberRepository.save(other)

            var book = Book.builder().title("T").image("I").isbn("ISBN").build()
            book = bookRepository.save(book)
            var reading = Reading.builder().member(other).book(book).isPublic(true).build()
            reading = readingRepository.save(reading)

            var card = Card.builder()
                .member(other)
                .reading(reading)
                .content("C")
                .image("I")
                .isPublic(true)
                .build()
            card = cardRepository.save(card)

            mockMvc.perform(
                patch("/api/v1/cards/${card.id}/visibility")
                    .param("isPublic", "false")
            )
                .andExpect(status().isForbidden)
        }
    }

    private fun createCard(isPublic: Boolean): Card {
        var book = Book.builder().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        var reading = Reading.builder().member(getMockMember()).book(book).isPublic(true).build()
        reading = readingRepository.save(reading)

        val card = Card.builder()
            .member(getMockMember())
            .reading(reading)
            .content("Content")
            .image("Img")
            .isPublic(isPublic)
            .build()
        return cardRepository.save(card)
    }
}
