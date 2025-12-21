package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.veri.be.domain.book.dto.book.AddBookRequest;
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest;
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardIntegrationTest extends IntegrationTestSupport {

    @Autowired
    StorageService storageService;

    @Nested
    @DisplayName("GET /api/v1/cards/my/count")
    class GetCardCount {
        @Test
        @DisplayName("정상 조회")
        void countSuccess() throws Exception {
            mockMvc.perform(get("/api/v1/cards/my/count"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards")
    class CreateCard {
        @Test
        @DisplayName("공개 독서에 카드 생성")
        void createCardSuccess() throws Exception {
            Integer readingId = createReading(true);
            CardCreateRequest request = new CardCreateRequest("Content", "https://img.com", readingId.longValue(), true);

            mockMvc.perform(post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result.cardId").exists());
        }

        @Test
        @DisplayName("content/image 누락")
        void createValidationFail() throws Exception {
            CardCreateRequest request = new CardCreateRequest(null, null, 1L, true);

            mockMvc.perform(post("/api/v1/cards")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/{cardId}")
    class GetCard {
        @Test
        @DisplayName("공개 카드 조회")
        void getCardSuccess() throws Exception {
            Integer readingId = createReading(true);
            Long cardId = createCard(readingId.longValue());

            mockMvc.perform(get("/api/v1/cards/" + cardId))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.id").value(cardId));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/cards/{cardId}")
    class UpdateCard {
        @Test
        @DisplayName("소유 카드 수정")
        void updateCardSuccess() throws Exception {
            Integer readingId = createReading(true);
            Long cardId = createCard(readingId.longValue());
            CardUpdateRequest request = new CardUpdateRequest("Updated Content", "https://newimg.com");

            mockMvc.perform(patch("/api/v1/cards/" + cardId)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.content").value("Updated Content"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/cards/{cardId}")
    class DeleteCard {
        @Test
        @DisplayName("정상 삭제")
        void deleteCardSuccess() throws Exception {
            Integer readingId = createReading(true);
            Long cardId = createCard(readingId.longValue());

            mockMvc.perform(delete("/api/v1/cards/" + cardId))
                    .andExpect(status().isNoContent());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/cards/my")
    class GetMyCards {
        @Test
        @DisplayName("size/page 최소 위반")
        void invalidPage() throws Exception {
            mockMvc.perform(get("/api/v1/cards/my")
                            .param("page", "0"))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("정렬 파라미터 오류")
        void invalidSort() throws Exception {
            mockMvc.perform(get("/api/v1/cards/my")
                            .param("sort", "INVALID"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/cards/image")
    class PresignedUrl {
        @Test
        @DisplayName("presigned URL 발급")
        void urlSuccess() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 1000L);

            mockMvc.perform(post("/api/v1/cards/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("용량 초과")
        void urlTooLarge() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 10 * 1024 * 1024L); // 10MB

            mockMvc.perform(post("/api/v1/cards/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("이미지 타입 아님")
        void urlInvalidType() throws Exception {
            PresignedUrlRequest request = new PresignedUrlRequest("text/plain", 1000L);

            mockMvc.perform(post("/api/v1/cards/image")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v2/cards/image")
    class PresignedUrlV2 {
        @Test
        @DisplayName("presigned POST form 발급")
        void urlSuccess() throws Exception {
            mockMvc.perform(post("/api/v2/cards/image"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.url").exists());
        }
    }

    private Integer createReading(boolean isPublic) throws Exception {
        AddBookRequest addRequest = new AddBookRequest("T", "I", "A", "P", "ISBN" + System.currentTimeMillis(), isPublic);
        String responseString = mockMvc.perform(post("/api/v2/bookshelf")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(addRequest)))
                .andReturn().getResponse().getContentAsString();
        return com.jayway.jsonpath.JsonPath.read(responseString, "$.result.memberBookId");
    }

    private Long createCard(Long readingId) throws Exception {
        CardCreateRequest request = new CardCreateRequest("Content", "https://img.com", readingId, true);
        String responseString = mockMvc.perform(post("/api/v1/cards")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andReturn().getResponse().getContentAsString();
        return ((Number) com.jayway.jsonpath.JsonPath.read(responseString, "$.result.cardId")).longValue();
    }
}
