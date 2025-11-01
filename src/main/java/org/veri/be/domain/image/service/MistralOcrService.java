package org.veri.be.domain.image.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.domain.image.exception.ImageErrorInfo;
import org.veri.be.domain.image.repository.OcrResultRepository;
import org.veri.be.global.exception.http.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Slf4j
@Service
public class MistralOcrService extends OcrService {

    private final RestClient restClient;
    private final String mistralApiKey;
    private final String mistralOcrModel;

    public MistralOcrService(
            RestClient.Builder restClientBuilder,
            @Value("${mistral.ocr.url}") String mistralApiUrl,
            @Value("${mistral.ocr.key}") String mistralApiKey,
            @Value("${mistral.ocr.model}") String mistralOcrModel,
            OcrResultRepository ocrResultRepository
    ) {
        super(ocrResultRepository);
        this.restClient = restClientBuilder.baseUrl(mistralApiUrl).build();
        this.mistralApiKey = mistralApiKey;
        this.mistralOcrModel = mistralOcrModel;
    }

    @Override
    protected String serviceName() {
        return "Mistral";
    }

    @Override
    protected String doExtract(String imageUrl) {
        // Todo. 전처리 이미지 사용 위해 0.5초 대기, 테스트 후 제거 고려
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
        }

        String preprocessedImageUrl = this.getPreprocessedUrl(imageUrl);

        // Todo. 전처리 / 원본 동시 요청, 전처리 유효 테스트 중에만 사용
        CompletableFuture<String> preFuture =
                CompletableFuture.supplyAsync(() -> callMistralApi(preprocessedImageUrl));

        CompletableFuture<String> origFuture =
                CompletableFuture.supplyAsync(() -> callMistralApi(imageUrl));

        String preText = null;
        String origText = null;

        try {
            preText = preFuture.join();
        } catch (CompletionException e) {
            log.warn("Mistral OCR(preprocessed) 실패: {}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        try {
            origText = origFuture.join();
        } catch (CompletionException e) {
            log.warn("Mistral OCR(원본) 실패: {}",
                    e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
        }

        if (preText != null) {
            saveOcrResult(imageUrl, preprocessedImageUrl, preText);
        }
        if (origText != null) {
            saveOcrResult(imageUrl, null, origText);
        }

        if (preText != null) {
            return preText;
        } else if (origText != null) {
            return origText;
        } else {
            log.error("Mistral OCR 전처리/원본 모두 실패");
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
        }
    }

    private String callMistralApi(String targetImageUrl) throws RestClientException {
        MistralOcrRequest requestBody = MistralOcrRequest.builder()
                .model(mistralOcrModel)
                .document(new DocumentPayload(new ImageUrlPayload(targetImageUrl)))
                .build();

        MistralOcrResponse response = restClient.post()
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + mistralApiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(requestBody)
                .retrieve()
                .body(MistralOcrResponse.class);

        if (response == null || response.pages == null || response.pages.isEmpty()) {
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
        }

        return response.pages.stream()
                .map(Page::getMarkdown)
                .reduce("", (acc, pageText) -> acc + "\n" + pageText)
                .trim();
    }

    // --- DTOs ---
    @Data
    @Builder
    private static class MistralOcrRequest {
        private String model;
        private DocumentPayload document;
    }

    @Data
    @AllArgsConstructor
    private static class DocumentPayload {
        private ImageUrlPayload image_url;
    }

    @Data
    @AllArgsConstructor
    private static class ImageUrlPayload {
        private String url;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class MistralOcrResponse {
        private String model;
        private List<Page> pages;
    }

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    private static class Page {
        private int index;
        private String markdown;
    }
}
