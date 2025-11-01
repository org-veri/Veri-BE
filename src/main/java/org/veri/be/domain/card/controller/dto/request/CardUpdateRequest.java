package org.veri.be.domain.card.controller.dto.request;


import jakarta.validation.constraints.NotNull;

public record CardUpdateRequest(
        @NotNull(message = "변경할 카드 내용은 필수입니다.")
        String content
) {
}
