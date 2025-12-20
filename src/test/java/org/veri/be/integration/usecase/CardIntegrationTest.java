package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
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
    }
}
