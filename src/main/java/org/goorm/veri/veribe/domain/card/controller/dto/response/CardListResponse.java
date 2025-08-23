package org.goorm.veri.veribe.domain.card.controller.dto.response;

import org.goorm.veri.veribe.domain.card.repository.dto.CardListItem;
import org.goorm.veri.veribe.domain.card.repository.dto.CardFeedItem;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.springframework.data.domain.Page;

import java.util.List;

public record CardListResponse(
        List<CardFeedItem> cards,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public CardListResponse(Page<CardFeedItem> pageData) {
        this(
                pageData.getContent(),
                pageData.getNumber() + 1, // 페이지 번호는 0부터 시작하므로 +1
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }

    public static CardListResponse ofOwn(Page<CardListItem> pageData, Member member) {
        return new CardListResponse(
                pageData.getContent().stream()
                        .map(card -> new CardFeedItem(card, member))
                        .toList(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
