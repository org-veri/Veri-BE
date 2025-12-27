package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class SocialReadingIntegrationTest extends IntegrationTestSupport {

    @Autowired BookRepository bookRepository;
    @Autowired ReadingRepository readingRepository;

    @Nested
    @DisplayName("GET /api/v2/bookshelf/popular")
    class GetPopular {
        @Test
        @DisplayName("인기 도서 10개 조회")
        void getPopularSuccess() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("최근 일주일 내 추가 도서 없음")
        void getPopularEmpty() throws Exception {
            // Assuming clean DB or no recent adds
            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.books").isEmpty());
        }

        @Test
        @DisplayName("인기 도서 최대 10개 제한")
        void getPopularLimit() throws Exception {
            // Create 12 readings for 12 different books
            for (int i = 0; i < 12; i++) {
                createReading(true, getMockMember(), "ISBN" + i);
            }

            mockMvc.perform(get("/api/v2/bookshelf/popular"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.books.length()").value(org.hamcrest.Matchers.lessThanOrEqualTo(10)));
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/{readingId}")
    class GetReadingDetail {
        @Test
        @DisplayName("공개 독서 상세")
        void getReadingDetailSuccess() throws Exception {
            Reading reading = createReading(true, getMockMember(), "ISBN-DETAIL");

            mockMvc.perform(get("/api/v2/bookshelf/" + reading.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.memberBookId").value(reading.getId()));
        }

        @Test
        @DisplayName("비공개 + 소유자 조회")
        void getPrivateOwner() throws Exception {
            Reading reading = createReading(false, getMockMember(), "ISBN-PRIVATE");

            mockMvc.perform(get("/api/v2/bookshelf/" + reading.getId()))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("비공개 + 타인 접근")
        void getPrivateOther() throws Exception {
            org.veri.be.domain.member.entity.Member other = org.veri.be.domain.member.entity.Member.builder()
                    .email("o").nickname("o").profileImageUrl("p").providerId("p").providerType(org.veri.be.domain.member.entity.enums.ProviderType.KAKAO).build();
            memberRepository.save(other);
            Reading reading = createReading(false, other, "ISBN-OTHER");

            mockMvc.perform(get("/api/v2/bookshelf/" + reading.getId()))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("존재하지 않는 ID")
        void getNotFound() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/999"))
                    .andExpect(status().isBadRequest()); // As per checklist expectation (BookErrorCode.BAD_REQUEST)
        }
    }

    private Reading createReading(boolean isPublic, org.veri.be.domain.member.entity.Member member, String isbn) {
        Book book = Book.builder().title("T").image("I").isbn(isbn).build();
        book = bookRepository.save(book);
        Reading reading = Reading.builder()
                .member(member)
                .book(book)
                .isPublic(isPublic)
                .build();
        return readingRepository.save(reading);
    }
}
