package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.card.entity.Card;

public class CardConverter {

    public static CardDetailResponse toCardDetailResponse(Card card) {
        return new CardDetailResponse(
                card.getId(),
                card.getContent(),
                card.getImage(),
                new CardDetailResponse.BookInfo(card.getMemberBook().getBook())
        );
    }
}
