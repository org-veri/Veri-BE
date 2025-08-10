package org.goorm.veri.veribe.domain.card.controller.dto;


import jakarta.validation.constraints.NotNull;

public record CardUpdateRequest(
        @NotNull(message = "카드 내용은 필수입니다. 변경하지 않을시 기존 내용을 입력해주세요")
        String content,

        @NotNull(message = "이미지 URL은 필수입니다. 변경하지 않을시 기존 url을 입력해주세요")
        String imageUrl
) {
}
