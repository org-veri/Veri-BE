package org.goorm.veri.veribe.api.social;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.controller.dto.response.*;
import org.goorm.veri.veribe.domain.card.controller.enums.CardSortType;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.goorm.veri.veribe.domain.card.service.CardQueryService;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "소셜")
@Tag(name = "카드")
@RequestMapping("/api/v1/cards")
@RestController
@RequiredArgsConstructor
public class SocialCardController {

    private final CardCommandService cardCommandService;
    private final CardQueryService cardQueryService;

    @Operation(summary = "전체 카드 목록 조회", description = "모든 사용자의 공개된 카드 목록을 페이지네이션과 정렬 기준으로 조회합니다.")
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

    @Operation(summary = "카드 공개 여부 수정", description = "독서가 비공개 상태라면 카드는 공개할 수 없습니다.")
    @PatchMapping("/{cardId}/visibility")
    public ApiResponse<CardVisibilityUpdateResponse> modifyVisibility(
            @PathVariable Long cardId,
            @RequestParam boolean isPublic
    ) {
        return ApiResponse.ok(cardCommandService.modifyVisibility(cardId, isPublic));
    }
}
