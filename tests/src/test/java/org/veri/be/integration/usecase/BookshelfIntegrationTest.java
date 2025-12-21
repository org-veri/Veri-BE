package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.veri.be.domain.book.client.BookSearchClient;
import org.veri.be.domain.book.dto.book.AddBookRequest;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Import(org.veri.be.integration.SharedTestConfig.class)
class BookshelfIntegrationTest extends IntegrationTestSupport {

    @Autowired
    BookSearchClient bookSearchClient;

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    class GetMyBooks {
        @Test
        @DisplayName("기본 조회")
        void getMyBooksSuccess() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("상태/정렬 필터링")
        void getMyBooksFiltered() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my")
                            .param("statuses", "READING", "DONE")
                            .param("sortType", "SCORE"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("잘못된 sortType")
        void getMyBooksInvalidSort() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my")
                            .param("sortType", "INVALID"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("page 최소 위반")
        void getMyBooksInvalidPage() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my")
                            .param("page", "0"))
                    .andExpect(status().isBadRequest());
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

        @Test
        @DisplayName("동일 도서 중복 추가")
        void addDuplicateBook() throws Exception {
            AddBookRequest request = new AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false);

            // First add
            mockMvc.perform(post("/api/v2/bookshelf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated());

            // Second add
            mockMvc.perform(post("/api/v2/bookshelf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated()); // Still 201, but idempotent
        }

        @Test
        @DisplayName("잘못된 ISBN 등 유효성")
        void addBookValidationFail() throws Exception {
            AddBookRequest request = new AddBookRequest(null, null, null, null, null, false);

            mockMvc.perform(post("/api/v2/bookshelf")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest()); // Assuming validation exists
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

        @Test
        @DisplayName("page 범위 위반")
        void searchInvalidPage() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/search")
                            .param("query", "Test")
                            .param("page", "0"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/count")
    class GetCount {
        @Test
        @DisplayName("완독 수 존재")
        void countSuccess() throws Exception {
            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("완독 없음")
        void countZero() throws Exception {
            // Setup: ensure no done books
            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(0));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/modify")
    class ModifyBook {
        @Test
        @DisplayName("점수/기간 모두 수정")
        void modifySuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(patch("/api/v2/bookshelf/" + id + "/modify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": 4.5, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("점수 0.5 단위 위반")
        void modifyScoreInvalid() throws Exception {
            mockMvc.perform(patch("/api/v2/bookshelf/1/modify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": 4.4, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("startedAt/endedAt 역전")
        void modifyTimeReverse() throws Exception {
            mockMvc.perform(patch("/api/v2/bookshelf/1/modify")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": 4.5, \"startedAt\": \"2024-01-02T00:00:00\", \"endedAt\": \"2024-01-01T00:00:00\"}"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/rate")
    class RateBook {
        @Test
        @DisplayName("정상 별점 등록")
        void rateSuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(patch("/api/v2/bookshelf/" + id + "/rate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": 3.5}"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("범위 위반")
        void rateInvalid() throws Exception {
            mockMvc.perform(patch("/api/v2/bookshelf/1/rate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": 5.5}"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("null 로 점수 제거")
        void rateNull() throws Exception {
            Integer readingId = createReading();

            mockMvc.perform(patch("/api/v2/bookshelf/" + readingId + "/rate")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{\"score\": null}"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/status")
    class StatusBook {
        @Test
        @DisplayName("독서 시작")
        void startSuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(patch("/api/v2/bookshelf/" + id + "/status/start"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("독서 완료")
        void overSuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(patch("/api/v2/bookshelf/" + id + "/status/over"))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("이미 DONE 상태")
        void statusAlreadyDone() throws Exception {
            Integer readingId = createReading();

            // Complete reading
            mockMvc.perform(patch("/api/v2/bookshelf/" + readingId + "/status/over"))
                    .andExpect(status().isNoContent());

            // Start again
            mockMvc.perform(patch("/api/v2/bookshelf/" + readingId + "/status/start"))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/visibility")
    class VisibilityBook {
        @Test
        @DisplayName("공개로 전환")
        void visibilitySuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(patch("/api/v2/bookshelf/" + id + "/visibility")
                            .param("isPublic", "true"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.isPublic").value(true));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/bookshelf/{id}")
    class DeleteBook {
        @Test
        @DisplayName("정상 삭제")
        void deleteSuccess() throws Exception {
            Integer id = createReading();
            mockMvc.perform(delete("/api/v2/bookshelf/" + id))
                    .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("존재하지 않는 ID")
        void deleteNotFound() throws Exception {
            mockMvc.perform(delete("/api/v2/bookshelf/999"))
                    .andExpect(status().isBadRequest());
        }
    }

    private Integer createReading() throws Exception {
        AddBookRequest addRequest = new AddBookRequest("Title", "Img", "Author", "Pub", "ISBN" + System.currentTimeMillis(), false);
        String responseString = mockMvc.perform(post("/api/v2/bookshelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(responseString, "$.result.memberBookId");
    }
}
