package org.goorm.veri.veribe.domain.book.dto.book;

public record BookRequest(
        String title,
        String image,
        String author,
        String publisher,
        String isbn
) {}
