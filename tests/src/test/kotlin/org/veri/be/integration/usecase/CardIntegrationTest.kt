package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.dto.book.AddBookRequest
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.steps.BookshelfSteps
import org.veri.be.support.steps.CardSteps

class CardIntegrationTest : IntegrationTestSupport() {

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    inner class GetCardCount {
        @Test
        @DisplayName("요청하면 → 200을 반환한다")
        fun countSuccess() {
            CardSteps.getMyCardCount(mockMvc)
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards")
    inner class CreateCard {
        @Test
        @DisplayName("공개 독서에 카드 생성하면 → 201을 반환한다")
        fun createCardSuccess() {
            val readingId = createReading(true)
            val request = CardCreateRequest("Content", "https://img.com", readingId.toLong(), true)

            CardSteps.requestCreateCard(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.cardId").exists())
        }

        @Test
        @DisplayName("content/image 누락이면 → 400을 반환한다")
        fun createValidationFail() {
            val request = CardCreateRequest(null, null, 1L, true)

            CardSteps.requestCreateCard(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    inner class GetCard {
        @Test
        @DisplayName("공개 카드를 조회하면 → 결과를 반환한다")
        fun getCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())

            CardSteps.getCardDetail(mockMvc, cardId)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(cardId))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    inner class UpdateCard {
        @Test
        @DisplayName("소유 카드를 수정하면 → 결과를 반환한다")
        fun updateCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())
            val request = CardUpdateRequest("Updated Content", "https://newimg.com")

            CardSteps.updateCard(mockMvc, objectMapper, cardId, request)
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.content").value("Updated Content"))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    inner class DeleteCard {
        @Test
        @DisplayName("카드를 삭제하면 → 204를 반환한다")
        fun deleteCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())

            CardSteps.deleteCard(mockMvc, cardId)
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    inner class GetMyCards {
        @Test
        @DisplayName("size/page 최소 위반이면 → 400을 반환한다")
        fun invalidPage() {
            CardSteps.getMyCards(mockMvc, mapOf("page" to "0"))
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("정렬 파라미터 오류면 → 400을 반환한다")
        fun invalidSort() {
            CardSteps.getMyCards(mockMvc, mapOf("sort" to "INVALID"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    inner class PresignedUrl {
        @Test
        @DisplayName("presigned URL을 발급하면 → 200을 반환한다")
        fun urlSuccess() {
            CardSteps.requestPresignedUrl(mockMvc, objectMapper, "/api/v1/cards/image", 1000L)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("용량이 초과되면 → 400을 반환한다")
        fun urlTooLarge() {
            CardSteps.requestPresignedUrl(mockMvc, objectMapper, "/api/v1/cards/image", 10 * 1024 * 1024L)
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("이미지 타입이 아니면 → 400을 반환한다")
        fun urlInvalidType() {
            val request = PresignedUrlRequest("text/plain", 1000L)

            mockMvc.perform(
                org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v1/cards/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v2/cards/image")
    inner class PresignedUrlV2 {
        @Test
        @DisplayName("presigned POST form을 발급하면 → 200을 반환한다")
        fun urlSuccess() {
            mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post("/api/v2/cards/image"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.url").exists())
        }
    }

    private fun createReading(isPublic: Boolean): Int {
        val addRequest = AddBookRequest("T", "I", "A", "P", "ISBN${System.currentTimeMillis()}", isPublic)
        return BookshelfSteps.createReadingId(mockMvc, objectMapper, addRequest)
    }

    private fun createCard(readingId: Long): Long {
        val request = CardCreateRequest("Content", "https://img.com", readingId, true)
        return CardSteps.createCard(mockMvc, objectMapper, request)
    }
}
