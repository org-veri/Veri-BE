package org.veri.be.book.dto.book;

import lombok.Builder;
import lombok.Getter;
import org.veri.be.book.entity.Book;

@Getter
@Builder
public class BookResponse {
    private String title;
    private String author;
    private String imageUrl;
    private String publisher;
    private String isbn;

    public static BookResponse from(Book book) {
        if (book == null) {
            return null;
        }
        return BookResponse.builder()
                .title(book.getTitle())
                .author(book.getAuthor())
                .imageUrl(book.getImage())
                .publisher(book.getPublisher())
                .isbn(book.getIsbn())
                .build();
    }

    public static BookResponse from(NaverBookItem response) {
        return BookResponse.builder()
                .author(response.getAuthor())
                .imageUrl(response.getImage())
                .title(response.getTitle())
                .publisher(response.getPublisher())
                .isbn(response.getIsbn())
                .build();
    }
}
