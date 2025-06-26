package org.goorm.veri.veribe.domain.card.controller;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v2/cards")
@RestController
@RequiredArgsConstructor
public class CardControllerV2 {

    private final CardCommandService cardCommandService;

    @PostMapping("/image")
    public DefaultResponse<PresignedPostForm> uploadCardImageV2(@RequestBody PresignedUrlRequest request) {
        return DefaultResponse.ok(cardCommandService.getPresignedPost(request));
    }
}
