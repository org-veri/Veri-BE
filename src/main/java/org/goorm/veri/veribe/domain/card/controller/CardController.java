package org.goorm.veri.veribe.domain.card.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.card.controller.dto.*;
import org.goorm.veri.veribe.domain.card.controller.dto.request.CardCreateRequest;
import org.goorm.veri.veribe.domain.card.controller.dto.request.CardUpdateRequest;
import org.goorm.veri.veribe.domain.card.controller.dto.response.*;
import org.goorm.veri.veribe.domain.card.controller.enums.CardSortType;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.goorm.veri.veribe.domain.card.service.CardQueryService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "독서 카드 API")
@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
public class CardController {

    private final CardCommandService cardCommandService;
    private final CardQueryService cardQueryService;

    @Operation(summary = "카드 생성", description = "카드를 새로 생성합니다.")
    @PostMapping
    public ApiResponse<CardCreateResponse> createCard(
            @RequestBody CardCreateRequest request,
            @AuthenticatedMember Member member) {
        Long cardId = cardCommandService.createCard(
                member,
                request.content(),
                request.imageUrl(),
                request.memberBookId()
        );
        return ApiResponse.created(new CardCreateResponse(cardId));
    }

    @Operation(summary = "내 카드 개수 조회", description = "로그인한 사용자의 카드 개수를 조회합니다.")
    @GetMapping("/my/count")
    public ApiResponse<Integer> getMyCardCount(@AuthenticatedMember Member member) {
        return ApiResponse.ok(cardQueryService.getOwnedCardCount(member.getId()));
    }

    @Operation(summary = "내 카드 목록 조회", description = "로그인한 사용자의 카드 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<CardListResponse> getMyCards(
            @AuthenticatedMember Member member,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        CardSortType sortType = CardSortType.from(sort);
        return ApiResponse.ok(
                CardListResponse.ofOwn(
                        cardQueryService.getOwnedCards(member.getId(), page - 1, size, sortType), member)
        );
    }

    @Operation(summary = "카드 목록 조회", description = "카드 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping()
    public ApiResponse<CardListResponse> getCards(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort
    ) {
        CardSortType sortType = CardSortType.from(sort);
        return ApiResponse.ok(
                new CardListResponse(cardQueryService.getAllCards(page - 1, size, sortType))
        );
    }

    @Operation(summary = "카드 상세 조회", description = "카드 ID로 카드의 상세 정보를 조회합니다.")
    @GetMapping("/{cardId}")
    public ApiResponse<CardDetailResponse> getCard(@PathVariable Long cardId) {
        Card card = cardQueryService.getCardById(cardId);
        return ApiResponse.ok(CardConverter.toCardDetailResponse(card));
    }

    @Operation(summary = "카드 수정", description = "카드 ID로 카드를 수정합니다. 본인 소유 카드만 수정할 수 있습니다.")
    @PatchMapping("/{cardId}")
    public ApiResponse<CardUpdateResponse> updateCard(
            @PathVariable Long cardId,
            @RequestBody CardUpdateRequest request
    ) {
        Card response = cardCommandService.updateCard(
                cardId,
                request.content()
        );
        return ApiResponse.ok(CardConverter.toCardUpdateResponse(response));
    }

    @Operation(summary = "카드 삭제", description = "카드 ID로 카드를 삭제합니다. 본인 소유 카드만 삭제할 수 있습니다.")
    @DeleteMapping("/{cardId}")
    public ApiResponse<Void> deleteCard(@PathVariable Long cardId) {
        cardCommandService.deleteCard(cardId);
        return ApiResponse.noContent();
    }

    /**
     * 요청한 이미지 타입과 크기에 맞는 presigned url을 반환합니다.
     * <p>
     * 클라이언트에서 해당 url에 PUT 방식으로 이미지 업로드
     */
    @Operation(summary = "카드 이미지 presigned URL 발급", description = "요청한 이미지 타입과 크기에 맞는 presigned URL을 발급합니다. 해당 URL로 PUT 방식 업로드가 가능합니다.")
    @PostMapping("/image")
    public ApiResponse<PresignedUrlResponse> uploadCardImage(@RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(cardCommandService.getPresignedUrl(request));
    }

    @Operation(summary = "ocr을 위한 이미지 presigned URL 발급", description = "요청한 이미지 타입과 크기에 맞는 presigned URL을 발급합니다. 해당 URL로 PUT 방식 업로드가 가능합니다.")
    @PostMapping("/image/ocr")
    public ApiResponse<PresignedUrlResponse> uploadCardImageForOcr(@RequestBody PresignedUrlRequest request) {
        return ApiResponse.ok(cardCommandService.getPresignedUrlForOcr(request));
    }
}
