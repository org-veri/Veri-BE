package org.veri.be.unit.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

import java.time.Duration;
import java.util.concurrent.Executor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.image.client.OcrPort;
import org.veri.be.domain.image.entity.OcrResult;
import org.veri.be.domain.image.exception.ImageErrorCode;
import org.veri.be.domain.image.repository.OcrResultRepository;
import org.veri.be.domain.image.service.MistralOcrService;
import org.veri.be.lib.time.SleepSupport;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class MistralOcrServiceTest {

    @Mock
    OcrResultRepository ocrResultRepository;

    @Mock
    OcrPort ocrClient;

    @Mock
    SleepSupport sleepSupport;

    @Captor
    ArgumentCaptor<OcrResult> resultCaptor;

    MistralOcrService service;

    @BeforeEach
    void setUp() {
        Executor executor = Runnable::run;
        service = new MistralOcrService(ocrResultRepository, ocrClient, sleepSupport, executor);
    }

    @Nested
    @DisplayName("extract")
    class Extract {

        @Test
        @DisplayName("원본 이미지 OCR 성공 시 결과를 저장한다")
        void savesOriginalResult() throws Exception {
            doNothing().when(sleepSupport).sleep(any(Duration.class));
            given(ocrClient.requestOcr("https://example.com/ocr/image.png")).willReturn("text");

            String result = service.extract("https://example.com/ocr/image.png");

            assertThat(result).isEqualTo("text");
            verify(ocrResultRepository).save(resultCaptor.capture());
            OcrResult saved = resultCaptor.getValue();
            assertThat(saved.getImageUrl()).isEqualTo("https://example.com/ocr/image.png");
            assertThat(saved.getPreProcessedUrl()).isNull();
            assertThat(saved.getResultText()).isEqualTo("text");
        }

        @Test
        @DisplayName("원본 실패 후 전처리 성공 시 전처리 URL로 저장한다")
        void fallsBackToPreprocessedUrl() throws Exception {
            doNothing().when(sleepSupport).sleep(any(Duration.class));
            given(ocrClient.requestOcr("https://example.com/ocr/image.png"))
                    .willThrow(new RuntimeException("fail"));
            given(ocrClient.requestOcr("https://example.com/ocr-preprocessed/image.jpg"))
                    .willReturn("text");

            String result = service.extract("https://example.com/ocr/image.png");

            assertThat(result).isEqualTo("text");
            verify(ocrResultRepository).save(resultCaptor.capture());
            OcrResult saved = resultCaptor.getValue();
            assertThat(saved.getPreProcessedUrl()).isEqualTo("https://example.com/ocr-preprocessed/image.jpg");
        }

        @Test
        @DisplayName("원본과 전처리 모두 실패하면 예외가 발생한다")
        void throwsWhenBothFail() throws Exception {
            doNothing().when(sleepSupport).sleep(any(Duration.class));
            given(ocrClient.requestOcr(any(String.class))).willThrow(new RuntimeException("fail"));

            ExceptionAssertions.assertApplicationException(
                    () -> service.extract("https://example.com/ocr/image.png"),
                    ImageErrorCode.OCR_PROCESSING_FAILED
            );
        }

        @Test
        @DisplayName("슬립 중 인터럽트가 발생하면 예외가 발생한다")
        void throwsWhenInterrupted() throws Exception {
            org.mockito.Mockito.doThrow(new InterruptedException("interrupt"))
                    .when(sleepSupport).sleep(any(Duration.class));

            ExceptionAssertions.assertApplicationException(
                    () -> service.extract("https://example.com/ocr/image.png"),
                    ImageErrorCode.OCR_PROCESSING_FAILED
            );
        }
    }
}
