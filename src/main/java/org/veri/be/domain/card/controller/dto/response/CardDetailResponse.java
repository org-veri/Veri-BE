package org.veri.be.domain.card.controller.dto.response;

import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.common.dto.MemberProfileResponse;

import java.time.LocalDateTime;

public record CardDetailResponse(
        Long id,
        MemberProfileResponse memberProfileResponse,
        String content,
        String imageUrl,
        LocalDateTime createdAt,
        BookInfo book,
        Boolean isPublic
) {

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
