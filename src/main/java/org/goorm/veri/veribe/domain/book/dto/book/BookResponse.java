package org.goorm.veri.veribe.domain.book.dto.book;

import lombok.Builder;
import lombok.Getter;
import org.goorm.veri.veribe.domain.book.entity.Book;

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
}
