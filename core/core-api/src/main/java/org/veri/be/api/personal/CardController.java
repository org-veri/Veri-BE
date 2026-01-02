package org.veri.be.api.personal;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.veri.be.global.auth.context.AuthenticatedMember;
import org.veri.be.global.auth.context.CurrentMemberInfo;
import org.veri.be.domain.card.controller.dto.request.CardCreateRequest;
import org.veri.be.domain.card.controller.dto.request.CardUpdateRequest;
import org.veri.be.domain.card.controller.dto.response.CardCreateResponse;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardListResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.controller.enums.CardSortType;
import org.veri.be.domain.card.service.CardCommandService;
import org.veri.be.domain.card.service.CardQueryService;
import org.veri.be.lib.response.ApiResponse;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.bind.annotation.*;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ApplicationException;

@Tag(name = "독서 카드")
@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
@Validated
public class CardController {

    private final CardCommandService cardCommandService;
    private final CardQueryService cardQueryService;

    @Operation(summary = "카드 생성", description = "카드를 새로 생성합니다. 독서가 비공개 상태라면 카드는 무조건 비공개로 생성됩니다.")
    @PostMapping
    public ApiResponse<CardCreateResponse> createCard(
            @RequestBody @Valid CardCreateRequest request,
            @AuthenticatedMember CurrentMemberInfo memberInfo
    ) {
        Long cardId = cardCommandService.createCard(
                memberInfo.id(),
                request.content(),
                request.imageUrl(),
                request.memberBookId(),
                request.isPublic()
        );
        return ApiResponse.created(new CardCreateResponse(cardId));
    }

    @Operation(summary = "내 카드 개수 조회", description = "로그인한 사용자의 카드 개수를 조회합니다.")
    @GetMapping("/my/count")
    public ApiResponse<Integer> getMyCardCount(@AuthenticatedMember CurrentMemberInfo memberInfo) {
        return ApiResponse.ok(cardQueryService.getOwnedCardCount(memberInfo.id()));
    }

    @Operation(summary = "내 카드 목록 조회", description = "로그인한 사용자의 카드 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
    @GetMapping("/my")
    public ApiResponse<CardListResponse> getMyCards(
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "newest") String sort,
            @AuthenticatedMember CurrentMemberInfo memberInfo
    ) {
        if (page < 1 || size < 1) {
            throw ApplicationException.of(CommonErrorCode.INVALID_REQUEST);
        }
        CardSortType sortType = CardSortType.from(sort);
        return ApiResponse.ok(CardListResponse.ofOwn(cardQueryService.getOwnedCards(memberInfo.id(), page - 1, size, sortType))
        );
    }

    @Operation(summary = "카드 상세 조회", description = "카드 ID로 카드의 상세 정보를 조회합니다.")
    @GetMapping("/{cardId}")
    public ApiResponse<CardDetailResponse> getCard(
            @PathVariable Long cardId,
            @AuthenticatedMember CurrentMemberInfo memberInfo
    ) {
        return ApiResponse.ok(cardQueryService.getCardDetail(cardId, memberInfo.id()));
    }

    @Operation(summary = "카드 수정", description = "카드 ID로 카드를 수정합니다. 본인 소유 카드만 수정할 수 있습니다.")
    @PatchMapping("/{cardId}")
    public ApiResponse<CardUpdateResponse> updateCard(
            @PathVariable Long cardId,
            @RequestBody @Valid CardUpdateRequest request,
            @AuthenticatedMember CurrentMemberInfo memberInfo
    ) {
        CardUpdateResponse response = cardCommandService.updateCard(
                memberInfo.id(),
                cardId,
                request.content(),
                request.imageUrl()
        );
        return ApiResponse.ok(response);
    }

    @Operation(summary = "카드 삭제", description = "카드 ID로 카드를 삭제합니다. 본인 소유 카드만 삭제할 수 있습니다.")
    @DeleteMapping("/{cardId}")
    public ApiResponse<Void> deleteCard(
            @PathVariable Long cardId,
            @AuthenticatedMember CurrentMemberInfo memberInfo
    ) {
        cardCommandService.deleteCard(memberInfo.id(), cardId);
        return ApiResponse.noContent();
    }

    /**
     * 요청한 이미지 타입과 크기에 맞는 presigned url을 반환합니다.
     * <p>
     * 클라이언트에서 해당 url에 PUT 방식으로 이미지 업로드
     */
    @Operation(summary = "카드 이미지 presigned URL 발급", description = "요청한 이미지 타입과 크기에 맞는 presigned URL을 발급합니다. 해당 URL로 PUT 방식 업로드가 가능합니다.")
    @PostMapping("/image")
    public ApiResponse<PresignedUrlResponse> uploadCardImage(@RequestBody @Valid PresignedUrlRequest request) {
        return ApiResponse.ok(cardCommandService.getPresignedUrl(request));
    }

    @Operation(summary = "ocr을 위한 이미지 presigned URL 발급", description = "요청한 이미지 타입과 크기에 맞는 presigned URL을 발급합니다. 해당 URL로 PUT 방식 업로드가 가능합니다.")
    @PostMapping("/image/ocr")
    public ApiResponse<PresignedUrlResponse> uploadCardImageForOcr(@RequestBody @Valid PresignedUrlRequest request) {
        return ApiResponse.ok(cardCommandService.getPresignedUrlForOcr(request));
    }
}
