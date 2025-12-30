package org.veri.be.image.client;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.List;

@Component
public class MistralOcrClient {

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
                throw new MistralOcrClientException("Mistral OCR response is empty.");
            }

            return response.pages.stream()
                    .map(Page::getMarkdown)
                    .reduce("", (acc, pageText) -> acc + "\n" + pageText)
                    .trim();
        } catch (RestClientException _) {
            throw new MistralOcrClientException("Mistral OCR request failed.");
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
        @JsonProperty("image_url")
        private ImageUrlPayload imageUrl;
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
