package org.veri.be.unit.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.Arguments
import org.junit.jupiter.params.provider.MethodSource
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.image.entity.OcrResult
import org.veri.be.domain.image.repository.OcrResultRepository
import org.veri.be.domain.image.service.AbstractOcrService
import org.veri.be.domain.image.service.OcrService
import java.util.stream.Stream

@ExtendWith(MockitoExtension::class)
class AbstractOcrServiceTest {

    @Mock
    private lateinit var ocrResultRepository: OcrResultRepository

    @Captor
    private lateinit var resultCaptor: ArgumentCaptor<OcrResult>

    private fun testService(): TestOcrService = TestOcrService()

    private inner class TestOcrService : AbstractOcrService(ocrResultRepository) {
        fun exposePreprocessedUrl(imageUrl: String): String = getPreprocessedUrl(imageUrl)

        override fun serviceName(): String = "Test"

        override fun doExtract(imageUrl: String): String {
            saveOcrResult(imageUrl, null, "text")
            return "text"
        }
    }

    @Nested
    @DisplayName("getPreprocessedUrl")
    inner class GetPreprocessedUrl {

        @ParameterizedTest
        @MethodSource("org.veri.be.unit.image.AbstractOcrServiceTest#preprocessedUrlCases")
        @DisplayName("입력 경로가 주어지면 → 전처리 URL을 생성한다")
        fun buildsPreprocessedUrl(input: String, expected: String) {
            val service = testService()

            val result = service.exposePreprocessedUrl(input)

            assertThat(result).isEqualTo(expected)
        }
    }

    @Nested
    @DisplayName("saveOcrResult")
    inner class SaveOcrResult {

        @Test
        @DisplayName("OCR 결과를 저장하면 → 결과를 저장한다")
        fun savesResult() {
            val service: OcrService = testService()

            service.extract("https://example.com/ocr/image.png")

            then(ocrResultRepository).should().save(resultCaptor.capture())
            val saved = resultCaptor.value
            assertThat(saved.imageUrl).isEqualTo("https://example.com/ocr/image.png")
            assertThat(saved.resultText).isEqualTo("text")
            assertThat(saved.ocrService).isEqualTo("Test")
        }
    }

    companion object {
        @JvmStatic
        fun preprocessedUrlCases(): Stream<Arguments> {
            return Stream.of(
                Arguments.of(
                    "https://example.com/ocr/image.png",
                    "https://example.com/ocr-preprocessed/image.jpg"
                ),
                Arguments.of(
                    "https://example.com/ocr/image",
                    "https://example.jpg"
                ),
                Arguments.of(
                    "ocr/image",
                    "ocr/image.jpg"
                )
            )
        }
    }
}
