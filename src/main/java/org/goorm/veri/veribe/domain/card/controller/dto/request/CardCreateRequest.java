package org.goorm.veri.veribe.domain.card.controller.dto.request;

public record CardCreateRequest(
        String content,
        String imageUrl,
        Long memberBookId
) {
}
