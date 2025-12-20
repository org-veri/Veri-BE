package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.book.dto.book.AddBookRequest;
import org.veri.be.domain.book.dto.book.NaverBookItem;
import org.veri.be.domain.book.dto.book.NaverBookResponse;
import org.veri.be.integration.IntegrationTestSupport;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@Import(org.veri.be.integration.SharedTestConfig.class)
class BookshelfIntegrationTest extends IntegrationTestSupport {

    @Autowired BookSearchClient bookSearchClient;

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    class GetMyBooks {
        @Test
        @DisplayName("기본 조회")
        void getMyBooksSuccess() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v2/bookshelf")
    class AddBook {
        @Test
        @DisplayName("새 도서 + 책장 추가")
        void addBookSuccess() throws Exception {
            AddBookRequest request = new AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false);
            
            mockMvc.perform(post("/api/v2/bookshelf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result.memberBookId").exists());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/search")
    class SearchBooks {
        @Test
        @DisplayName("키워드 검색 성공")
        void searchSuccess() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/search")
                            .param("query", "Test"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.books[0].title").value("Stub Book Title"));
        }
    }
}
