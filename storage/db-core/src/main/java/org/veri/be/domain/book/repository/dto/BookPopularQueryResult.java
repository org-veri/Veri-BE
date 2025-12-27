package org.veri.be.domain.book.repository.dto;

public record BookPopularQueryResult(
        String image,
        String title,
        String author,
        String publisher,
        String isbn
) {
}
