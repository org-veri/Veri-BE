package org.goorm.veri.veribe.domain.card.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.Duration;

@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
public class CardController {

    private final StorageService storageService;

    @PostMapping("/image")
    public DefaultResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        String contentType = request.contentType();

        if (!contentType.startsWith("image/")) {
            throw new CardException(CardErrorCode.UNSUPPORTED_IMAGE_TYPE);
        }

        return DefaultResponse.ok(
                storageService.generatePresignedUrl(contentType, Duration.ofMinutes(5))
        );
    }
}
