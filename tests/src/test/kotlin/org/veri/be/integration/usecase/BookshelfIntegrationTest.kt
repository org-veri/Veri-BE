package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.context.annotation.Import
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.dto.book.AddBookRequest
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.integration.SharedTestConfig
import org.veri.be.support.steps.BookshelfSteps

@Import(SharedTestConfig::class)
class BookshelfIntegrationTest : IntegrationTestSupport() {

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my")
    inner class GetMyBooks {
        @Test
        @DisplayName("기본 조회하면 → 200을 반환한다")
        fun getMyBooksSuccess() {
            BookshelfSteps.getMyBooks(mockMvc)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("상태/정렬 필터링이면 → 200을 반환한다")
        fun getMyBooksFiltered() {
            BookshelfSteps.getMyBooks(
                mockMvc,
                mapOf(
                    "statuses" to listOf("READING", "DONE"),
                    "sortType" to listOf("SCORE")
                )
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("잘못된 sortType이면 → 400을 반환한다")
        fun getMyBooksInvalidSort() {
            BookshelfSteps.getMyBooks(mockMvc, mapOf("sortType" to listOf("INVALID")))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("page 최소 위반이면 → 400을 반환한다")
        fun getMyBooksInvalidPage() {
            BookshelfSteps.getMyBooks(mockMvc, mapOf("page" to listOf("0")))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v2/bookshelf")
    inner class AddBook {
        @Test
        @DisplayName("새 도서를 책장에 추가하면 → 201을 반환한다")
        fun addBookSuccess() {
            val request = AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false)

            BookshelfSteps.addBook(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.memberBookId").exists())
        }

        @Test
        @DisplayName("동일 도서 중복 추가면 → 201을 반환한다")
        fun addDuplicateBook() {
            val request = AddBookRequest("Title", "Img", "Author", "Pub", "ISBN123", false)

            BookshelfSteps.addBook(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)

            BookshelfSteps.addBook(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("유효성 실패면 → 400을 반환한다")
        fun addBookValidationFail() {
            val request = AddBookRequest(null, null, null, null, null, false)

            BookshelfSteps.addBook(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/search")
    inner class SearchBooks {
        @Test
        @DisplayName("키워드 검색하면 → 결과를 반환한다")
        fun searchSuccess() {
            BookshelfSteps.searchBooks(mockMvc, mapOf("query" to "Test"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.books[0].title").value("Stub Book Title"))
        }

        @Test
        @DisplayName("page 범위 위반이면 → 400을 반환한다")
        fun searchInvalidPage() {
            BookshelfSteps.searchBooks(
                mockMvc,
                mapOf(
                    "query" to "Test",
                    "page" to "0"
                )
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v2/bookshelf/my/count")
    inner class GetCount {
        @Test
        @DisplayName("완독 수가 존재하면 → 200을 반환한다")
        fun countSuccess() {
            BookshelfSteps.getMyCount(mockMvc)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("완독이 없으면 → 0을 반환한다")
        fun countZero() {
            BookshelfSteps.getMyCount(mockMvc)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value(0))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/modify")
    inner class ModifyBook {
        @Test
        @DisplayName("점수/기간을 수정하면 → 204를 반환한다")
        fun modifySuccess() {
            val id = createReading()
            BookshelfSteps.modifyReading(
                mockMvc,
                id,
                "{\"score\": 4.5, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"
            )
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("점수 0.5 단위 위반이면 → 400을 반환한다")
        fun modifyScoreInvalid() {
            BookshelfSteps.modifyReading(
                mockMvc,
                1,
                "{\"score\": 4.4, \"startedAt\": \"2024-01-01T00:00:00\", \"endedAt\": \"2024-01-02T00:00:00\"}"
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("startedAt/endedAt 역전이면 → 400을 반환한다")
        fun modifyTimeReverse() {
            BookshelfSteps.modifyReading(
                mockMvc,
                1,
                "{\"score\": 4.5, \"startedAt\": \"2024-01-02T00:00:00\", \"endedAt\": \"2024-01-01T00:00:00\"}"
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/rate")
    inner class RateBook {
        @Test
        @DisplayName("정상 별점을 등록하면 → 204를 반환한다")
        fun rateSuccess() {
            val id = createReading()
            BookshelfSteps.rateReading(mockMvc, id, "{\"score\": 3.5}")
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("범위 위반이면 → 400을 반환한다")
        fun rateInvalid() {
            BookshelfSteps.rateReading(mockMvc, 1, "{\"score\": 5.5}")
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("null 로 점수 제거하면 → 204를 반환한다")
        fun rateNull() {
            val readingId = createReading()

            BookshelfSteps.rateReading(mockMvc, readingId, "{\"score\": null}")
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/status")
    inner class StatusBook {
        @Test
        @DisplayName("독서를 시작하면 → 204를 반환한다")
        fun startSuccess() {
            val id = createReading()
            BookshelfSteps.startReading(mockMvc, id)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("독서를 완료하면 → 204를 반환한다")
        fun overSuccess() {
            val id = createReading()
            BookshelfSteps.finishReading(mockMvc, id)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("이미 DONE 상태면 → 204를 반환한다")
        fun statusAlreadyDone() {
            val readingId = createReading()

            BookshelfSteps.finishReading(mockMvc, readingId)
                .andExpect(status().isNoContent)

            BookshelfSteps.startReading(mockMvc, readingId)
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v2/bookshelf/{id}/visibility")
    inner class VisibilityBook {
        @Test
        @DisplayName("공개로 전환하면 → 200을 반환한다")
        fun visibilitySuccess() {
            val id = createReading()
            BookshelfSteps.updateVisibility(mockMvc, id, true)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.isPublic").value(true))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v2/bookshelf/{id}")
    inner class DeleteBook {
        @Test
        @DisplayName("정상 삭제하면 → 204를 반환한다")
        fun deleteSuccess() {
            val id = createReading()
            BookshelfSteps.deleteReading(mockMvc, id)
                .andExpect(status().isNoContent)
        }

        @Test
        @DisplayName("존재하지 않는 ID면 → 404를 반환한다")
        fun deleteNotFound() {
            BookshelfSteps.deleteReading(mockMvc, 999)
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
        return BookshelfSteps.createReadingId(mockMvc, objectMapper, addRequest)
    }
}
