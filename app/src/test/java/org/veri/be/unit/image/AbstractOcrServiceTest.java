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

        @Test
        @DisplayName("ocr 경로와 확장자를 변경한다")
        void convertsToPreprocessedUrl() {
            TestOcrService service = testService();

            String result = service.exposePreprocessedUrl("https://example.com/ocr/image.png");

            assertThat(result).isEqualTo("https://example.com/ocr-preprocessed/image.jpg");
        }

        @Test
        @DisplayName("확장자가 없으면 jpg를 붙인다")
        void addsJpgWhenNoExtension() {
            TestOcrService service = testService();

            String result = service.exposePreprocessedUrl("https://example.com/ocr/image");

            assertThat(result).isEqualTo("https://example.jpg");
        }

        @Test
        @DisplayName("점이 없으면 확장자를 추가한다")
        void addsJpgWhenNoDots() {
            TestOcrService service = testService();

            String result = service.exposePreprocessedUrl("ocr/image");

            assertThat(result).isEqualTo("ocr/image.jpg");
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
