package org.veri.be.book.repository.dto;

public record BookPopularQueryResult(
        String image,
        String title,
        String author,
        String publisher,
        String isbn
) {
}
