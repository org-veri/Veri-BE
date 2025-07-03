package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.book.entity.MemberBook;

import java.time.LocalDateTime;

public record CardDetailResponse(
        Long id,
        String content,
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
        public BookInfo(MemberBook memberBook) {
            this(
                    memberBook.getId(),
                    memberBook.getBook().getTitle(),
                    memberBook.getBook().getImage(),
                    memberBook.getBook().getAuthor()
            );
        }
    }
}
