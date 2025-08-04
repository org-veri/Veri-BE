package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.card.entity.CardContentText;

public record CardCreateRequest(
        CardContentText content,
        String imageUrl,
        Long memberBookId
) {
}
