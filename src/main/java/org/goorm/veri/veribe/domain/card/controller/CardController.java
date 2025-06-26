package org.goorm.veri.veribe.domain.card.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.card.controller.dto.CardConverter;
import org.goorm.veri.veribe.domain.card.controller.dto.CardCreateRequest;
import org.goorm.veri.veribe.domain.card.controller.dto.CardCreateResponse;
import org.goorm.veri.veribe.domain.card.controller.dto.CardDetailResponse;
import org.goorm.veri.veribe.domain.card.controller.dto.CardListResponse;
import org.goorm.veri.veribe.domain.card.controller.enums.CardSortType;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.goorm.veri.veribe.domain.card.service.CardQueryService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/api")
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardCommandService cardCommandService;
    private final CardQueryService cardQueryService;

    @PostMapping
    public DefaultResponse<CardCreateResponse> createCard(
            @RequestBody CardCreateRequest request,
            @AuthenticatedMember Member member) {
        Long cardId = cardCommandService.createCard(
                member.getId(),
                request.content(),
                request.imageUrl(),
                request.memberBookId()
        );

        return DefaultResponse.created(new CardCreateResponse(cardId));
    }

    @GetMapping("/my")
    public DefaultResponse<CardListResponse> getMyCards(
            @AuthenticatedMember Member member,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        CardSortType sortType = CardSortType.from(sort);

        return DefaultResponse.ok(
                new CardListResponse(cardQueryService.getOwnedCards(member.getId(), page - 1, size, sortType))
        );
    }

    @GetMapping("/{cardId}")
    public DefaultResponse<CardDetailResponse> getCard(@PathVariable Long cardId) {
        Card card = cardQueryService.getCardById(cardId);
        return DefaultResponse.ok(CardConverter.toCardDetailResponse(card));
    }

    @DeleteMapping("/{cardId}")
    public DefaultResponse<Void> deleteCard(@PathVariable Long cardId,
                                            @AuthenticatedMember Member member) {

        cardCommandService.deleteCard(member.getId(), cardId);
        return DefaultResponse.noContent();
    }

    @PostMapping("/image")
    public DefaultResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return DefaultResponse.ok(cardCommandService.getPresignedUrl(request));
    }

    @PostMapping("v2/cards/image")
    public DefaultResponse<PresignedPostForm> uploadCardImageV2(@RequestBody PresignedUrlRequest request) {
        return DefaultResponse.ok(cardCommandService.getPresignedPost(request));
    }
}
