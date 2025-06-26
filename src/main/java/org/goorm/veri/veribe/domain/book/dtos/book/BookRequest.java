package org.goorm.veri.veribe.domain.book.dtos.book;

public record BookRequest(
        String title,
        String image,
        String author,
        String publisher,
        String isbn
) {}
