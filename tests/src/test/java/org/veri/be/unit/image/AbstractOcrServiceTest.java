package org.veri.be.unit.image;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.image.entity.OcrResult;
import org.veri.be.domain.image.repository.OcrResultRepository;
import org.veri.be.domain.image.service.AbstractOcrService;
import org.veri.be.domain.image.service.OcrService;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

@ExtendWith(MockitoExtension.class)
class AbstractOcrServiceTest {

    @Mock
    OcrResultRepository ocrResultRepository;

    @Captor
    ArgumentCaptor<OcrResult> resultCaptor;

    private TestOcrService testService() {
        return new TestOcrService();
    }

    private class TestOcrService extends AbstractOcrService {
        TestOcrService() {
            super(AbstractOcrServiceTest.this.ocrResultRepository);
        }

        String exposePreprocessedUrl(String imageUrl) {
            return getPreprocessedUrl(imageUrl);
        }

        @Override
        protected String serviceName() {
            return "Test";
        }

        @Override
        protected String doExtract(String imageUrl) {
            saveOcrResult(imageUrl, null, "text");
            return "text";
        }
    }

    @Nested
    @DisplayName("getPreprocessedUrl")
    class GetPreprocessedUrl {

        @ParameterizedTest
        @MethodSource("preprocessedUrlCases")
        @DisplayName("입력 경로에 따라 전처리 URL을 생성한다")
        void buildsPreprocessedUrl(String input, String expected) {
            TestOcrService service = testService();

            String result = service.exposePreprocessedUrl(input);

            assertThat(result).isEqualTo(expected);
        }

        private static Stream<Arguments> preprocessedUrlCases() {
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
            );
        }
    }

    @Nested
    @DisplayName("saveOcrResult")
    class SaveOcrResult {

        @Test
        @DisplayName("OCR 결과를 저장한다")
        void savesResult() {
            OcrService service = testService();

            service.extract("https://example.com/ocr/image.png");

            verify(ocrResultRepository).save(resultCaptor.capture());
            OcrResult saved = resultCaptor.getValue();
            assertThat(saved.getImageUrl()).isEqualTo("https://example.com/ocr/image.png");
            assertThat(saved.getResultText()).isEqualTo("text");
            assertThat(saved.getOcrService()).isEqualTo("Test");
        }
    }
}
