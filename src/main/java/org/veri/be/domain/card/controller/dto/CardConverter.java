package org.veri.be.domain.card.controller.dto;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.card.controller.dto.response.CardDetailResponse;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.member.entity.Member;

@Component
public class CardConverter {

    public static CardDetailResponse toCardDetailResponse(Card card, Member viewer) {
        return CardDetailResponse.from(card, viewer);
    }

    public static CardUpdateResponse toCardUpdateResponse(Card card) {
        return CardUpdateResponse.from(card);
    }
}
