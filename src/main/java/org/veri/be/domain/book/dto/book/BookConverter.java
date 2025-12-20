package org.veri.be.domain.book.dto.book;

import org.veri.be.domain.book.entity.Book;

import java.util.List;

public class BookConverter {

    private BookConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static BookResponse toBookResponse(NaverBookItem response) {
        return BookResponse.from(response);
    }

    public static BookSearchResponse toBookSearchResponse(NaverBookResponse naverResponse) {
        return BookSearchResponse.from(naverResponse);
    }

    public static List<BookPopularResponse> toBookPopularResponse(List<Book> books) {
        return BookPopularResponse.from(books);
    }
}
