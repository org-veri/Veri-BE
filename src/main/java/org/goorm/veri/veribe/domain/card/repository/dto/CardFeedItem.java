package org.goorm.veri.veribe.domain.card.repository.dto;

import org.goorm.veri.veribe.domain.common.dto.MemberProfileResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;

import java.time.LocalDateTime;

public record CardFeedItem(
        Long cardId,
        MemberProfileResponse member,
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
        this(cardId, MemberProfileResponse.from(member), bookTitle, content, image, created, isPublic);
    }

    public CardFeedItem(CardListItem item, Member member) {
        this(
                item.getCardId(),
                MemberProfileResponse.from(member),
                item.getBookTitle(),
                item.getContent(),
                item.getImage(),
                item.getCreated(),
                item.isPublic()
        );
    }
}
