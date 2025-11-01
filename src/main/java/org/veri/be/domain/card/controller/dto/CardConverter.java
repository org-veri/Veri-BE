package org.veri.be.domain.card.controller.dto;

import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.common.dto.MemberProfileResponse;

public class CardConverter {

    public static CardDetailResponse toCardDetailResponse(Card card) {
        if (card == null) {
            return null;
        }

        return new CardDetailResponse(
                card.getId(),
                MemberProfileResponse.from(card.getMember()),
                card.getContent(),
                card.getImage(),
                card.getCreatedAt(),
                CardDetailResponse.BookInfo.from(card.getReading()),
                card.getIsPublic()
        );
    }

    public static CardUpdateResponse toCardUpdateResponse(Card card) {
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
