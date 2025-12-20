package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.image.service.OcrService;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ImageIntegrationTest extends IntegrationTestSupport {

    @Autowired OcrService ocrService;

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    class Ocr {
        @Test
        @DisplayName("정상 OCR + 저장")
        void ocrSuccess() throws Exception {
            mockMvc.perform(post("/api/v0/images/ocr")
                            .param("imageUrl", "https://example.com/img.png"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value("Stub OCR Result"));
        }

        @Test
        @DisplayName("OCR 서비스 예외")
        void ocrException() throws Exception {
            mockMvc.perform(post("/api/v0/images/ocr")
                            .param("imageUrl", "https://example.com/error.png"))
                    .andExpect(status().isInternalServerError());
        }

        @Test
        @DisplayName("URL 파라미터 누락")
        void missingParam() throws Exception {
            mockMvc.perform(post("/api/v0/images/ocr"))
                    .andExpect(status().isBadRequest());
        }
    }
}
