package org.goorm.veri.veribe.domain.card.controller.dto;

import org.goorm.veri.veribe.domain.book.entity.Book;

public record CardDetailResponse(
        Long id,
        String content,
        String imageUrl,
        BookInfo book
) {

    record BookInfo(
            String title,
            String coverImageUrl,
            String author
    ) {
        public BookInfo(Book book) {
            this(
                    book.getTitle(),
                    book.getImage(),
                    book.getAuthor()
            );
        }
    }
}
