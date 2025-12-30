package org.veri.be.domain.card.controller.dto.response;

import org.veri.be.book.entity.Reading;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.member.entity.Member;

import java.time.LocalDateTime;

public record CardDetailResponse(
        Long id,
        MemberProfileResponse memberProfileResponse,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        BookInfo book,
        Boolean isPublic,
        boolean mine
) {

    public static CardDetailResponse from(Card card, Member viewer) {
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
                BookInfo.from(card.getReading()),
                card.isPublic(),
                mine
        );
    }

    public record BookInfo(
            Long id,
            String title,
            String coverImageUrl,
            String author
    ) {
        public static BookInfo from(Reading reading) {
            if (reading == null) return null;
            return new BookInfo(
                    reading.getId(),
                    reading.getBook().getTitle(),
                    reading.getBook().getImage(),
                    reading.getBook().getAuthor()
            );
        }
    }
}
