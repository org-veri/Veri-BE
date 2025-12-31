package org.veri.be.integration.usecase

import com.jayway.jsonpath.JsonPath
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.book.dto.book.AddBookRequest
import org.veri.be.card.controller.dto.request.CardCreateRequest
import org.veri.be.card.controller.dto.request.CardUpdateRequest
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.service.StorageService
import org.veri.be.integration.IntegrationTestSupport

class CardIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var storageService: StorageService

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    inner class GetCardCount {
        @Test
        @DisplayName("정상 조회")
        fun countSuccess() {
            mockMvc.perform(get("/api/v1/cards/my/count"))
                .andExpect(status().isOk)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards")
    inner class CreateCard {
        @Test
        @DisplayName("공개 독서에 카드 생성")
        fun createCardSuccess() {
            val readingId = createReading(true)
            val request = CardCreateRequest("Content", "https://img.com", readingId.toLong(), true)

            mockMvc.perform(
                post("/api/v1/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result.cardId").exists())
        }

        @Test
        @DisplayName("content/image 누락")
        fun createValidationFail() {
            val request = CardCreateRequest(null, null, 1L, true)

            mockMvc.perform(
                post("/api/v1/cards")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    inner class GetCard {
        @Test
        @DisplayName("공개 카드 조회")
        fun getCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())

            mockMvc.perform(get("/api/v1/cards/$cardId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.id").value(cardId))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    inner class UpdateCard {
        @Test
        @DisplayName("소유 카드 수정")
        fun updateCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())
            val request = CardUpdateRequest("Updated Content", "https://newimg.com")

            mockMvc.perform(
                patch("/api/v1/cards/$cardId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.content").value("Updated Content"))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    inner class DeleteCard {
        @Test
        @DisplayName("정상 삭제")
        fun deleteCardSuccess() {
            val readingId = createReading(true)
            val cardId = createCard(readingId.toLong())

            mockMvc.perform(delete("/api/v1/cards/$cardId"))
                .andExpect(status().isNoContent)
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    inner class GetMyCards {
        @Test
        @DisplayName("size/page 최소 위반")
        fun invalidPage() {
            mockMvc.perform(
                get("/api/v1/cards/my")
                    .param("page", "0")
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        fun invalidSort() {
            mockMvc.perform(
                get("/api/v1/cards/my")
                    .param("sort", "INVALID")
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    inner class PresignedUrl {
        @Test
        @DisplayName("presigned URL 발급")
        fun urlSuccess() {
            val request = PresignedUrlRequest("image/png", 1000L)

            mockMvc.perform(
                post("/api/v1/cards/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("용량 초과")
        fun urlTooLarge() {
            val request = PresignedUrlRequest("image/png", 10 * 1024 * 1024L)

            mockMvc.perform(
                post("/api/v1/cards/image")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }

        @Test
        @DisplayName("이미지 타입 아님")
        fun urlInvalidType() {
            val request = PresignedUrlRequest("text/plain", 1000L)

            mockMvc.perform(
                post("/api/v1/cards/image")
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
        @DisplayName("presigned POST form 발급")
        fun urlSuccess() {
            mockMvc.perform(post("/api/v2/cards/image"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result.url").exists())
        }
    }

    private fun createReading(isPublic: Boolean): Int {
        val addRequest = AddBookRequest("T", "I", "A", "P", "ISBN${System.currentTimeMillis()}", isPublic)
        val responseString = mockMvc.perform(
            post("/api/v2/bookshelf")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(addRequest))
        )
            .andReturn().response.contentAsString
        return JsonPath.read(responseString, "$.result.memberBookId")
    }

    private fun createCard(readingId: Long): Long {
        val request = CardCreateRequest("Content", "https://img.com", readingId, true)
        val responseString = mockMvc.perform(
            post("/api/v1/cards")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
            .andReturn().response.contentAsString
        val cardId: Number = JsonPath.read(responseString, "$.result.cardId")
        return cardId.toLong()
    }
}
