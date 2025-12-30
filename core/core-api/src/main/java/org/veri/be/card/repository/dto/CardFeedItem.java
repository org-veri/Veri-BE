package org.veri.be.card.repository.dto;

import org.veri.be.member.entity.Member;
import org.veri.be.member.repository.dto.MemberProfileQueryResult;

import java.time.LocalDateTime;

public record CardFeedItem(
        Long cardId,
        MemberProfileQueryResult member,
        String bookTitle,
        String content,
        String image,
        LocalDateTime created,
        Boolean isPublic
) {

    public CardFeedItem(
            Long cardId,
            Member member,
            String bookTitle,
            String content,
            String image,
            LocalDateTime created,
            Boolean isPublic

    ) {
        this(cardId, MemberProfileQueryResult.from(member), bookTitle, content, image, created, isPublic);
    }

    public CardFeedItem(CardListItem item, Member member) {
        this(
                item.getCardId(),
                MemberProfileQueryResult.from(member),
                item.getBookTitle(),
                item.getContent(),
                item.getImage(),
                item.getCreated(),
                item.isPublic()
        );
    }
}
