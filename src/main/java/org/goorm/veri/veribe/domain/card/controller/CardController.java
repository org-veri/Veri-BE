package org.goorm.veri.veribe.domain.card.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.controller.dto.CardCreateRequest;
import org.goorm.veri.veribe.domain.card.controller.dto.CardCreateResponse;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardCommandService cardCommandService;

    @PostMapping
    public DefaultResponse<CardCreateResponse> createCard(@RequestBody CardCreateRequest request) {
        Long memberId = 1L; // Todo. SecurityContext 로 변경

        Long cardId = cardCommandService.createCard(
                memberId,
                request.content(),
                request.imageUrl(),
                request.memberBookId()
        );

        return DefaultResponse.created(new CardCreateResponse(cardId));
    }

    @DeleteMapping("/{cardId}")
    public DefaultResponse<Void> deleteCard(@PathVariable Long cardId) {
        Long memberId = 1L; // Todo. SecurityContext 로 변경

        cardCommandService.deleteCard(memberId, cardId);
        return DefaultResponse.noContent();
    }

    @PostMapping("/image")
    public DefaultResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return DefaultResponse.ok(cardCommandService.getPresignedUrl(request));
    }
}
