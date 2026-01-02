package org.veri.be.unit.image

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.Mockito.doNothing
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.image.client.OcrPort
import org.veri.be.domain.image.entity.OcrResult
import org.veri.be.domain.image.exception.ImageErrorCode
import org.veri.be.domain.image.repository.OcrResultRepository
import org.veri.be.domain.image.service.MistralOcrService
import org.veri.be.lib.time.SleepSupport
import org.veri.be.support.assertion.ExceptionAssertions
import java.time.Duration
import java.util.concurrent.Executor

@ExtendWith(MockitoExtension::class)
class MistralOcrServiceTest {

    @org.mockito.Mock
    private lateinit var ocrResultRepository: OcrResultRepository

    @org.mockito.Mock
    private lateinit var ocrClient: OcrPort

    @org.mockito.Mock
    private lateinit var sleepSupport: SleepSupport

    @org.mockito.Captor
    private lateinit var resultCaptor: ArgumentCaptor<OcrResult>

    private lateinit var service: MistralOcrService

    @BeforeEach
    fun setUp() {
        val executor: Executor = Executor { runnable -> runnable.run() }
        service = MistralOcrService(ocrResultRepository, ocrClient, sleepSupport, executor)
    }

    @Nested
    @DisplayName("extract")
    inner class Extract {

        @Test
        @DisplayName("원본 이미지 OCR 성공 시 → 결과를 저장한다")
        fun savesOriginalResult() {
            doNothing().`when`(sleepSupport).sleep(any(Duration::class.java))
            given(ocrClient.requestOcr("https://example.com/ocr/image.png")).willReturn("text")

            val result = service.extract("https://example.com/ocr/image.png")

            assertThat(result).isEqualTo("text")
            then(ocrResultRepository).should().save(resultCaptor.capture())
            val saved = resultCaptor.value
            assertThat(saved.imageUrl).isEqualTo("https://example.com/ocr/image.png")
            assertThat(saved.preProcessedUrl).isNull()
            assertThat(saved.resultText).isEqualTo("text")
        }

        @Test
        @DisplayName("원본 실패 후 전처리 성공 시 → 전처리 URL로 저장한다")
        fun fallsBackToPreprocessedUrl() {
            doNothing().`when`(sleepSupport).sleep(any(Duration::class.java))
            given(ocrClient.requestOcr("https://example.com/ocr/image.png"))
                .willThrow(RuntimeException("fail"))
            given(ocrClient.requestOcr("https://example.com/ocr-preprocessed/image.jpg"))
                .willReturn("text")

            val result = service.extract("https://example.com/ocr/image.png")

            assertThat(result).isEqualTo("text")
            then(ocrResultRepository).should().save(resultCaptor.capture())
            val saved = resultCaptor.value
            assertThat(saved.preProcessedUrl).isEqualTo("https://example.com/ocr-preprocessed/image.jpg")
        }

        @Test
        @DisplayName("원본과 전처리 모두 실패하면 → 예외가 발생한다")
        fun throwsWhenBothFail() {
            doNothing().`when`(sleepSupport).sleep(any(Duration::class.java))
            given(ocrClient.requestOcr(any(String::class.java))).willThrow(RuntimeException("fail"))

            ExceptionAssertions.assertApplicationException(
                { service.extract("https://example.com/ocr/image.png") },
                ImageErrorCode.OCR_PROCESSING_FAILED
            )
        }

        @Test
        @DisplayName("슬립 중 인터럽트가 발생하면 → 예외가 발생한다")
        fun throwsWhenInterrupted() {
            org.mockito.Mockito.doThrow(InterruptedException("interrupt"))
                .`when`(sleepSupport).sleep(any(Duration::class.java))

            ExceptionAssertions.assertApplicationException(
                { service.extract("https://example.com/ocr/image.png") },
                ImageErrorCode.OCR_PROCESSING_FAILED
            )
        }
    }
}
