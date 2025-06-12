package org.goorm.veri.veribe.domain.card.controller.dto;

public record CardCreateRequest(
        String content,
        String imageUrl,
        Long memberBookId
) {
}
