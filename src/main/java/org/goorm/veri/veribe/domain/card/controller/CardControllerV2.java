package org.goorm.veri.veribe.domain.card.controller;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.card.service.CardCommandService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
    @PostMapping("v2/cards/image")
    public DefaultResponse<PresignedPostForm> uploadCardImageV2() {
        return DefaultResponse.ok(cardCommandService.getPresignedPost());
    }
}
