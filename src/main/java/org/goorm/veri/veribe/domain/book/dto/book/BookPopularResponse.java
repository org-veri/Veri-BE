package org.goorm.veri.veribe.domain.book.dto.book;

public record BookPopularResponse (
        String image,
        String title,
        String author,
        String publisher,
        String isbn)
{}
