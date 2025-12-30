package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.image.service.OcrService
import org.veri.be.integration.IntegrationTestSupport

class ImageIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var ocrService: OcrService

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    inner class Ocr {
        @Test
        @DisplayName("정상 OCR + 저장")
        fun ocrSuccess() {
            mockMvc.perform(
                post("/api/v0/images/ocr")
                    .param("imageUrl", "https://example.com/img.png")
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("Stub OCR Result"))
        }

        @Test
        @DisplayName("OCR 서비스 예외")
        fun ocrException() {
            mockMvc.perform(
                post("/api/v0/images/ocr")
                    .param("imageUrl", "https://example.com/error.png")
            )
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("URL 파라미터 누락")
        fun missingParam() {
            mockMvc.perform(post("/api/v0/images/ocr"))
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/images/ocr")
    inner class OcrV1 {
        @Test
        @DisplayName("동일 URL 재업로드")
        fun duplicateUrl() {
            val url = "https://example.com/duplicate.png"

            mockMvc.perform(
                post("/api/v1/images/ocr")
                    .param("imageUrl", url)
            )
                .andExpect(status().isOk)

            mockMvc.perform(
                post("/api/v1/images/ocr")
                    .param("imageUrl", url)
            )
                .andExpect(status().isOk)
        }
    }
}
