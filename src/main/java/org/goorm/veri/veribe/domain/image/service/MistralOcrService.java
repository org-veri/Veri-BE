package org.goorm.veri.veribe.domain.image.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.transaction.Transactional;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.goorm.veri.veribe.domain.image.entity.Image;
import org.goorm.veri.veribe.domain.image.entity.OcrResult;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorInfo;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.goorm.veri.veribe.domain.image.repository.OcrResultRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
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
public class MistralOcrService {

    private final RestClient restClient;
    private final String mistralApiKey;
    private final String mistralOcrModel;
    private final OcrResultRepository ocrResultRepository;
    private final ImageRepository imageRepository;

    public MistralOcrService(
            RestClient.Builder restClientBuilder,
            @Value("${mistral.ocr.url}") String mistralApiUrl,
            @Value("${mistral.ocr.key}") String mistralApiKey,
            @Value("${mistral.ocr.model}") String mistralOcrModel,
            OcrResultRepository ocrResultRepository,
            ImageRepository imageRepository
    ) {
        this.restClient = restClientBuilder.baseUrl(mistralApiUrl).build();
        this.mistralApiKey = mistralApiKey;
        this.mistralOcrModel = mistralOcrModel;
        this.ocrResultRepository = ocrResultRepository;
        this.imageRepository = imageRepository;
    }

    /**
     * 이미지 URL을 받아 Mistral OCR API를 호출하고, 추출된 텍스트를 반환합니다.
     *
     * @param imageUrl OCR을 수행할 이미지의 URL
     * @return 추출된 텍스트 (String)
     */
    @Transactional
    public String extractTextFromImageUrl(String imageUrl, Member member) {
        insertImageUrl(imageUrl, member);

        MistralOcrRequest requestBody = MistralOcrRequest.builder()
                .model(mistralOcrModel)
                .document(new DocumentPayload(new ImageUrlPayload(imageUrl)))
                .build();

        try {
            MistralOcrResponse response = restClient.post()
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + mistralApiKey)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(requestBody)
                    .retrieve()
                    .onStatus(
                            status -> status.is4xxClientError() || status.is5xxServerError(),
                            (request, clientResponse) -> {
                                throw new RestClientException(
                                        String.format("Mistral API 호출 실패: Status %d, Body: %s",
                                                clientResponse.getStatusCode().value(),
                                                new String(clientResponse.getBody().readAllBytes()))
                                );
                            }
                    )
                    .body(MistralOcrResponse.class);

            if (response == null || response.pages == null || response.pages.isEmpty()) {
                throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
            }

            String extracted = response.pages.stream()
                    .map(Page::getMarkdown)
                    .reduce("", (acc, pageText) -> acc + "\n" + pageText).trim();

            OcrResult result = OcrResult.builder()
                    .resultText(extracted)
                    .imageUrl(imageUrl)
                    .ocrService("Mistral")
                    .build();
            ocrResultRepository.save(result);

            return extracted;


        } catch (RestClientException e) {
            log.error("Mistral OCR Error.", e);
            throw new InternalServerException(ImageErrorInfo.OCR_PROCESSING_FAILED);
        }

    }

    private void insertImageUrl(String imageUrl, Member member) {
        Image image = Image.builder()
                .member(member)
                .imageUrl(imageUrl)
                .build();
        imageRepository.save(image);
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
