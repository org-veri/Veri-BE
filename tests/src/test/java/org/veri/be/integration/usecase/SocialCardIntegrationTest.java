package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
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
            createCard(true);

            mockMvc.perform(get("/api/v1/cards"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.cards[0].cardId").exists());
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        void invalidSort() throws Exception {
            mockMvc.perform(get("/api/v1/cards")
                            .param("sort", "INVALID"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("page 초과")
        void pageOverflow() throws Exception {
            mockMvc.perform(get("/api/v1/cards")
                            .param("page", "1000"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.cards").isEmpty());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}/visibility")
    class ModifyVisibility {
        @Test
        @DisplayName("공개 -> 비공개")
        void toPrivate() throws Exception {
            Card card = createCard(true);

            mockMvc.perform(patch("/api/v1/cards/" + card.getId() + "/visibility")
                            .param("isPublic", "false"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isPublic").value(false));
        }

        @Test
        @DisplayName("비공개 -> 공개")
        void toPublic() throws Exception {
            // Need public reading
            Card card = createCard(true);
            card.changeVisibility(getMockMember(), false); // Set private first
            cardRepository.save(card);

            mockMvc.perform(patch("/api/v1/cards/" + card.getId() + "/visibility")
                            .param("isPublic", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isPublic").value(true));
        }

        @Test
        @DisplayName("비공개 독서에 속한 카드 공개 시도")
        void publicOnPrivateReading() throws Exception {
            // Create private reading
            Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
            book = bookRepository.save(book);
            Reading reading = Reading.builder().member(getMockMember()).book(book).isPublic(false).build();
            reading = readingRepository.save(reading);

            Card card = Card.builder()
                    .member(getMockMember())
                    .reading(reading)
                    .content("C")
                    .image("I")
                    .isPublic(false)
                    .build();
            card = cardRepository.save(card);

            mockMvc.perform(patch("/api/v1/cards/" + card.getId() + "/visibility")
                            .param("isPublic", "true"))
                    .andExpect(status().isForbidden()); // Should fail as reading is private
        }

        @Test
        @DisplayName("타인 카드")
        void otherCard() throws Exception {
            org.veri.be.domain.member.entity.Member other = org.veri.be.domain.member.entity.Member.builder()
                    .email("o").nickname("o").profileImageUrl("p").providerId("p").providerType(org.veri.be.domain.member.entity.enums.ProviderType.KAKAO).build();
            memberRepository.save(other);
            
            Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
            book = bookRepository.save(book);
            Reading reading = Reading.builder().member(other).book(book).isPublic(true).build();
            reading = readingRepository.save(reading);
            
            Card card = Card.builder().member(other).reading(reading).content("C").image("I").isPublic(true).build();
            card = cardRepository.save(card);

            mockMvc.perform(patch("/api/v1/cards/" + card.getId() + "/visibility")
                            .param("isPublic", "false"))
                    .andExpect(status().isForbidden());
        }
    }

    private Card createCard(boolean isPublic) {
        Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
        book = bookRepository.save(book);
        Reading reading = Reading.builder().member(getMockMember()).book(book).isPublic(true).build();
        reading = readingRepository.save(reading);
        
        Card card = Card.builder()
                .member(getMockMember())
                .reading(reading)
                .content("Content")
                .image("Img")
                .isPublic(isPublic)
                .build();
        return cardRepository.save(card);
    }
}