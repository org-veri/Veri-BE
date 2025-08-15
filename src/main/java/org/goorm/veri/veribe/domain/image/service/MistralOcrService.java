package org.goorm.veri.veribe.domain.image.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorInfo;
import org.goorm.veri.veribe.domain.image.repository.OcrResultRepository;
import org.goorm.veri.veribe.global.exception.http.InternalServerException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

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
    protected OcrResultPayload doExtract(String imageUrl) {
        String preprocessedImageUrl = this.getPreprocessedUrl(imageUrl);
        try {
            String text = callMistralApi(preprocessedImageUrl);
            return new OcrResultPayload(preprocessedImageUrl, text);
        } catch (RestClientException e1) {
            log.warn("Mistral OCR(preprocessed) 실패: {} -> 원본으로 재시도", e1.getMessage());
            try {
                String text = callMistralApi(imageUrl);
                return new OcrResultPayload(null, text);
            } catch (RestClientException e2) {
                log.error("Mistral OCR 원본도 실패: {}", e2.getMessage());
                throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
            }
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
