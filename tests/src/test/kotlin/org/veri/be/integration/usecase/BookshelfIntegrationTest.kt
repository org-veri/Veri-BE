package org.veri.be.integration.usecase

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Import
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.book.client.BookSearchClient
import org.veri.be.book.dto.book.AddBookRequest
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.integration.SharedTestConfig

@Import(SharedTestConfig::class)
class BookshelfIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookSearchClient: BookSearchClient

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    inner class GetMyBooks {
        @Test
        @DisplayName("기본 조회")
        fun getMyBooksSuccess() {
            mockMvc.perform(get("/api/v2/bookshelf/my"))
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("상태/정렬 필터링")
        fun getMyBooksFiltered() {
            mockMvc.perform(
                get("/api/v2/bookshelf/my")
                    .param("statuses", "READING", "DONE")
                    .param("sortType", "SCORE")
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("잘못된 sortType")
        fun getMyBooksInvalidSort() {
            mockMvc.perform(
                get("/api/v2/bookshelf/my")
                    .param("sortType", "INVALID")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("page 최소 위반")
        fun getMyBooksInvalidPage() {
            mockMvc.perform(
                get("/api/v2/bookshelf/my")
                    .param("page", "0")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v2/bookshelf")
    inner class AddBook {
        @Test
        @DisplayName("새 도서 + 책장 추가")
        fun addBookSuccess() {
            val request = AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.memberBookId").exists())
        }

        @Test
        @DisplayName("동일 도서 중복 추가")
        fun addDuplicateBook() {
            val request = AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("잘못된 ISBN 등 유효성")
        fun addBookValidationFail() {
            val request = AddBookRequest(null, null, null, null, null, false)

            mockMvc.perform(
                post("/api/v2/bookshelf")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/search")
    inner class SearchBooks {
        @Test
        @DisplayName("키워드 검색 성공")
        fun searchSuccess() {
            mockMvc.perform(
                get("/api/v2/bookshelf/search")
                    .param("query", "Test")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books[0].title").value("Stub Book Title"))
        }

        @Test
        @DisplayName("page 범위 위반")
        fun searchInvalidPage() {
            mockMvc.perform(
                get("/api/v2/bookshelf/search")
                    .param("query", "Test")
                    .param("page", "0")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/count")
    inner class GetCount {
        @Test
        @DisplayName("완독 수 존재")
        fun countSuccess() {
            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("완독 없음")
        fun countZero() {
            mockMvc.perform(get("/api/v2/bookshelf/my/count"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(0))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/modify")
    inner class ModifyBook {
        @Test
        @DisplayName("점수/기간 모두 수정")
        fun modifySuccess() {
            val id = createReading()
            mockMvc.perform(
                patch("/api/v2/bookshelf/$id/modify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"score\": 4.5, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"
                    )
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("점수 0.5 단위 위반")
        fun modifyScoreInvalid() {
            mockMvc.perform(
                patch("/api/v2/bookshelf/1/modify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"score\": 4.4, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"
                    )
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("startedAt/endedAt 역전")
        fun modifyTimeReverse() {
            mockMvc.perform(
                patch("/api/v2/bookshelf/1/modify")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(
                        "{\"score\": 4.5, \"startedAt\": \"2024-01-02T00:00:00\", \"endedAt\": \"2024-01-01T00:00:00\"}"
                    )
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/rate")
    inner class RateBook {
        @Test
        @DisplayName("정상 별점 등록")
        fun rateSuccess() {
            val id = createReading()
            mockMvc.perform(
                patch("/api/v2/bookshelf/$id/rate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"score\": 3.5}")
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("범위 위반")
        fun rateInvalid() {
            mockMvc.perform(
                patch("/api/v2/bookshelf/1/rate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"score\": 5.5}")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("null 로 점수 제거")
        fun rateNull() {
            val readingId = createReading()

            mockMvc.perform(
                patch("/api/v2/bookshelf/$readingId/rate")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("{\"score\": null}")
            )
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/status")
    inner class StatusBook {
        @Test
        @DisplayName("독서 시작")
        fun startSuccess() {
            val id = createReading()
            mockMvc.perform(patch("/api/v2/bookshelf/$id/status/start"))
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("독서 완료")
        fun overSuccess() {
            val id = createReading()
            mockMvc.perform(patch("/api/v2/bookshelf/$id/status/over"))
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("이미 DONE 상태")
        fun statusAlreadyDone() {
            val readingId = createReading()

            mockMvc.perform(patch("/api/v2/bookshelf/$readingId/status/over"))
                .andExpect(status().isNoContent)

            mockMvc.perform(patch("/api/v2/bookshelf/$readingId/status/start"))
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/visibility")
    inner class VisibilityBook {
        @Test
        @DisplayName("공개로 전환")
        fun visibilitySuccess() {
            val id = createReading()
            mockMvc.perform(
                patch("/api/v2/bookshelf/$id/visibility")
                    .param("isPublic", "true")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/bookshelf/{id}")
    inner class DeleteBook {
        @Test
        @DisplayName("정상 삭제")
        fun deleteSuccess() {
            val id = createReading()
            mockMvc.perform(delete("/api/v2/bookshelf/$id"))
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("존재하지 않는 ID")
        fun deleteNotFound() {
            mockMvc.perform(delete("/api/v2/bookshelf/999"))
                .andExpect(status().isNotFound)
        }
    }

    private fun createReading(): Int {
        val addRequest = AddBookRequest(
            "Title",
            "Img",
            "Author",
            "Pub",
            "ISBN${System.currentTimeMillis()}",
            false
        )
        val responseString = mockMvc.perform(
            post("/api/v2/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest))
        )
            .andReturn().response.contentAsString
        return JsonPath.read(responseString, "$.result.memberBookId")
    }
}
