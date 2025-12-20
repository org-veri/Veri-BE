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
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/{readingId}")
    class GetReadingDetail {
        @Test
        @DisplayName("공개 독서 상세")
        void getReadingDetailSuccess() throws Exception {
            Book book = Book.builder().title("T").image("I").isbn("ISBN").build();
            book = bookRepository.save(book);
            Reading reading = Reading.builder()
                    .member(getMockMember())
                    .book(book)
                    .isPublic(true)
                    .build();
            reading = readingRepository.save(reading);

            mockMvc.perform(get("/api/v2/bookshelf/" + reading.getId()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.memberBookId").value(reading.getId()));
        }
    }
}
