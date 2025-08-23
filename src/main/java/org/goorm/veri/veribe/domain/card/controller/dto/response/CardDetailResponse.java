package org.goorm.veri.veribe.domain.card.controller.dto.response;

import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;

import java.time.LocalDateTime;

public record CardDetailResponse(
        Long id,
        MemberProfile memberProfile,
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
