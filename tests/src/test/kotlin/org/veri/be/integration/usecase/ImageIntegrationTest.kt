package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.image.service.OcrService
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.steps.ImageSteps

class ImageIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var ocrService: OcrService

    @Nested
    @DisplayName("POST /api/v0/images/ocr")
    inner class Ocr {
        @Test
        @DisplayName("정상 OCR + 저장이면 → 200을 반환한다")
        fun ocrSuccess() {
            ImageSteps.ocrV0(mockMvc, "https://example.com/img.png")
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.result").value("Stub OCR Result"))
        }

        @Test
        @DisplayName("OCR 서비스 예외면 → 500을 반환한다")
        fun ocrException() {
            ImageSteps.ocrV0(mockMvc, "https://example.com/error.png")
                .andExpect(status().isInternalServerError)
        }

        @Test
        @DisplayName("URL 파라미터 누락이면 → 400을 반환한다")
        fun missingParam() {
            ImageSteps.ocrV0(mockMvc, null)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/images/ocr")
    inner class OcrV1 {
        @Test
        @DisplayName("동일 URL 재업로드면 → 200을 반환한다")
        fun duplicateUrl() {
            val url = "https://example.com/duplicate.png"

            ImageSteps.ocrV1(mockMvc, url)
                .andExpect(status().isOk)

            ImageSteps.ocrV1(mockMvc, url)
                .andExpect(status().isOk)
        }
    }
}
