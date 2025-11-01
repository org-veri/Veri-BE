package org.veri.be.domain.book.dto.book;

public record BookPopularResponse (
        String image,
        String title,
        String author,
        String publisher,
        String isbn)
{}
