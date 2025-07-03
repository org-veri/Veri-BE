package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.card.entity.Card;

public class CardConverter {

    public static CardDetailResponse toCardDetailResponse(Card card) {
        return new CardDetailResponse(
                card.getId(),
                card.getContent(),
                card.getImage(),
                card.getCreatedAt(),
                CardDetailResponse.BookInfo.from(card.getMemberBook())
        );
    }
}
