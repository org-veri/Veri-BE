package org.veri.be.domain.card.controller.dto.request;


import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

public record CardUpdateRequest(
        @NotNull(message = "변경할 카드 내용은 필수입니다.")
        String content,

        @URL(message = "유효한 이미지 URL이어야 합니다.")
        String imageUrl
) {
}
