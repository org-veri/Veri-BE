package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.card.entity.CardContentText;

import java.time.LocalDateTime;

public record CardDetailResponse(
        Long id,
        CardContentText content,
        String imageUrl,
        LocalDateTime createdAt,
        BookInfo book
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
