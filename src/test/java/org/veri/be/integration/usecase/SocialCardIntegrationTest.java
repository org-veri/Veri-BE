package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SocialCardIntegrationTest extends IntegrationTestSupport {

    @Autowired CardRepository cardRepository;
    @Autowired BookRepository bookRepository;
    @Autowired ReadingRepository readingRepository;

    @Nested
    @DisplayName("GET /api/v1/cards")
    class GetCardsFeed {
        @Test
        @DisplayName("전체 공개 카드 feed 최신순")
        void getCardsFeedSuccess() throws Exception {
            Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
            book = bookRepository.save(book);
            Reading reading = Reading.builder().member(getMockMember()).book(book).isPublic(true).build();
            reading = readingRepository.save(reading);
            
            Card card = Card.builder()
                    .member(getMockMember())
                    .reading(reading)
                    .content("Content")
                    .image("Img")
                    .isPublic(true)
                    .build();
            cardRepository.save(card);

            mockMvc.perform(get("/api/v1/cards"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.cards[0].cardId").exists());
        }
    }
}
