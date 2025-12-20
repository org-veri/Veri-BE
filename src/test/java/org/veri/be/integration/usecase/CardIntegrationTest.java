package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.integration.IntegrationTestSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class CardIntegrationTest extends IntegrationTestSupport {

    @Autowired StorageService storageService;

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
}
