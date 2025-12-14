package org.veri.be.domain.image.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.veri.be.domain.image.exception.ImageErrorInfo;
import org.veri.be.lib.exception.http.InternalServerException;

import java.util.List;

@Component
public class MistralOcrClient implements OcrClient {

    private final RestClient restClient;
    private final String mistralApiKey;
    private final String mistralOcrModel;

    public MistralOcrClient(
            RestClient.Builder restClientBuilder,
            @Value("${mistral.ocr.url}") String mistralApiUrl,
            @Value("${mistral.ocr.key}") String mistralApiKey,
            @Value("${mistral.ocr.model}") String mistralOcrModel
    ) {
        this.restClient = restClientBuilder.baseUrl(mistralApiUrl).build();
        this.mistralApiKey = mistralApiKey;
        this.mistralOcrModel = mistralOcrModel;
    }

    @Override
    public String requestOcr(String imageUrl) {
        try {
            MistralOcrRequest requestBody = MistralOcrRequest.builder()
                    .model(mistralOcrModel)
                    .document(new DocumentPayload(new ImageUrlPayload(imageUrl)))
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
        } catch (RestClientException e) {
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
        }
    }

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
