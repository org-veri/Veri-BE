package org.veri.be.domain.card.controller.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.member.entity.Member;

@Component
@RequiredArgsConstructor
public class CardConverter {

    public CardDetailResponse toCardDetailResponse(Card card, Member viewer) {
        if (card == null) {
            return null;
        }

        boolean mine = viewer != null && card.getMember().getId().equals(viewer.getId());

        return new CardDetailResponse(
                card.getId(),
                MemberProfileResponse.from(card.getMember()),
                card.getContent(),
                card.getImage(),
                card.getCreatedAt(),
                CardDetailResponse.BookInfo.from(card.getReading()),
                card.getIsPublic(),
                mine
        );
    }

    public CardUpdateResponse toCardUpdateResponse(Card card) {
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
