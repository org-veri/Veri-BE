package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;
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

    record BookInfo(
            Long id,
            String title,
            String coverImageUrl,
            String author
    ) {
        public static BookInfo from(MemberBook memberBook) {
            if (memberBook == null) return null;
            return new BookInfo(
                    memberBook.getId(),
                    memberBook.getBook().getTitle(),
                    memberBook.getBook().getImage(),
                    memberBook.getBook().getAuthor()
            );
        }
    }
}
