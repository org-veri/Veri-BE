package org.veri.be.domain.card.controller.dto.response;

import java.time.LocalDateTime;
import org.veri.be.domain.card.entity.Card;

public record CardUpdateResponse(
        Long id,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        CardDetailResponse.BookInfo book
) {
    public static CardUpdateResponse from(Card card) {
        return new CardUpdateResponse(
                card.getId(),
                card.getContent(),
                card.getImage(),
                card.getCreatedAt(),
                card.getUpdatedAt(),
                CardDetailResponse.BookInfo.from(card.getReading())
        );
    }
}
