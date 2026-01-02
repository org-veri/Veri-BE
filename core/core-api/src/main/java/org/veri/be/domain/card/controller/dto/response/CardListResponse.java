package org.veri.be.domain.card.controller.dto.response;

import org.springframework.data.domain.Page;
import org.veri.be.domain.card.repository.dto.CardFeedItem;
import org.veri.be.domain.card.repository.dto.CardListItem;
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;

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

    public static CardListResponse ofOwn(Page<CardListItem> pageData) {
        return new CardListResponse(
                pageData.getContent().stream()
                        .map(card -> new CardFeedItem(
                                card.getCardId(),
                                (MemberProfileQueryResult) null, // 소유자의 프로필 정보는 필요하지 않음
                                card.getBookTitle(),
                                card.getContent(),
                                card.getImage(),
                                card.getCreated(),
                                card.isPublic()
                        ))
                        .toList(),
                pageData.getNumber() + 1,
                pageData.getSize(),
                pageData.getTotalElements(),
                pageData.getTotalPages()
        );
    }
}
