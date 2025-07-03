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
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "독서 카드 API")
@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardCommandService cardCommandService;
    private final CardQueryService cardQueryService;

    @Operation(summary = "카드 생성", description = "카드를 새로 생성합니다.")
    @PostMapping
    public DefaultResponse<CardCreateResponse> createCard(
            @RequestBody CardCreateRequest request,
            @AuthenticatedMember Member member) {
        Long cardId = cardCommandService.createCard(
                member,
                request.content(),
                request.imageUrl(),
                request.memberBookId()
        );

        return DefaultResponse.created(new CardCreateResponse(cardId));
    }

    @Operation(summary = "내 카드 개수 조회", description = "로그인한 사용자의 카드 개수를 조회합니다.")
    @GetMapping("/my/count")
    public DefaultResponse<Integer> getMyCardCount(@AuthenticatedMember Member member) {
        return DefaultResponse.ok(cardQueryService.getOwnedCardCount(member.getId()));
    }

    @Operation(summary = "내 카드 목록 조회", description = "로그인한 사용자의 카드 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
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

    @Operation(summary = "카드 상세 조회", description = "카드 ID로 카드의 상세 정보를 조회합니다.")
    @GetMapping("/{cardId}")
    public DefaultResponse<CardDetailResponse> getCard(@PathVariable Long cardId) {
        Card card = cardQueryService.getCardById(cardId);
        return DefaultResponse.ok(CardConverter.toCardDetailResponse(card));
    }

    @Operation(summary = "카드 삭제", description = "카드 ID로 카드를 삭제합니다. 본인 소유 카드만 삭제할 수 있습니다.")
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
    @Operation(summary = "카드 이미지 presigned URL 발급", description = "요청한 이미지 타입과 크기에 맞는 presigned URL을 발급합니다. 해당 URL로 PUT 방식 업로드가 가능합니다.")
    @PostMapping("/image")
    public DefaultResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {

        return DefaultResponse.ok(cardCommandService.getPresignedUrl(request));
    }
}
