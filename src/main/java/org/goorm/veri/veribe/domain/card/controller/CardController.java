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

@RequestMapping("/api/v1/cards")
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

    @GetMapping("/my/count")
    public DefaultResponse<Integer> getMyCardCount(@AuthenticatedMember Member member) {
        return DefaultResponse.ok(cardQueryService.getOwnedCardCount(member.getId()));
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

    /**
     * 요청한 이미지 타입과 크기에 맞는 presigned url을 반환합니다.
     * <p>
     * 클라이언트에서 해당 url에 PUT 방식으로 이미지 업로드
     */
    @PostMapping("/image")
    public DefaultResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {

        return DefaultResponse.ok(cardCommandService.getPresignedUrl(request));
    }
}
