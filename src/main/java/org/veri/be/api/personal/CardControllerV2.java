package org.veri.be.api.personal;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.card.service.CardCommandService;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "독서 카드")
@RequestMapping("/api/v2/cards")
@RestController
@RequiredArgsConstructor
public class CardControllerV2 {

    private final CardCommandService cardCommandService;


    /**
     * image/* 타입의 1MB 이하 업로드를 위한 presigned post form을 반환합니다.
     * <p>
     * 클라이언트에서 form-data 에 실제 이미지와 Content-Type을 수정하여 POST 방식 업로드
     */
    @Operation(summary = "카드 presigned post form 발급", description = "image/* 타입의 1MB 이하 업로드를 위한 presigned post form을 발급합니다. 클라이언트에서 form-data에 실제 이미지와 Content-Type을 수정하여 POST 방식 업로드가 가능합니다.")
    @PostMapping("/image")
    public ApiResponse<PresignedPostForm> uploadCardImageV2() {
        return ApiResponse.ok(cardCommandService.getPresignedPost());
    }
}
