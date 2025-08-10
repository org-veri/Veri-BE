package org.goorm.veri.veribe.domain.card.controller.dto;

import java.time.LocalDateTime;

public record CardUpdateResponse(
        Long id,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CardDetailResponse.BookInfo book
) {
}
