package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.card.controller.dto.response.CardDetailResponse;
import org.goorm.veri.veribe.domain.card.controller.dto.response.CardUpdateResponse;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;

public class CardConverter {

    public static CardDetailResponse toCardDetailResponse(Card card) {
        if (card == null) {
            return null;
        }

        return new CardDetailResponse(
                card.getId(),
                MemberProfile.from(card.getMember()),
                card.getContent(),
                card.getImage(),
                card.getCreatedAt(),
                CardDetailResponse.BookInfo.from(card.getReading()),
                card.isPublic()
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
