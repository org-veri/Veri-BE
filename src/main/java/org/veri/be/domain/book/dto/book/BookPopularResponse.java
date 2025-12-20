package org.veri.be.domain.book.dto.book;

import org.veri.be.domain.book.entity.Book;

import java.util.List;

public record BookPopularResponse (
        String image,
        String title,
        String author,
        String publisher,
        String isbn)
{
    public static BookPopularResponse from(Book book) {
        return new BookPopularResponse(
                book.getImage(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getIsbn()
        );
    }

    public static List<BookPopularResponse> from(List<Book> books) {
        return books.stream()
                .map(BookPopularResponse::from)
                .toList();
    }
}
