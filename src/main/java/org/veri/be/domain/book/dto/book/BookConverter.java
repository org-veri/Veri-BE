package org.veri.be.domain.book.dto.book;

import org.veri.be.domain.book.entity.Book;

import java.util.List;

public class BookConverter {

    public static BookResponse toBookResponse(NaverBookItem response) {
        return BookResponse.builder()
                .author(response.getAuthor())
                .imageUrl(response.getImage())
                .title(response.getTitle())
                .publisher(response.getPublisher())
                .isbn(response.getIsbn())
                .build();
    }

    public static BookSearchResponse toBookSearchResponse(NaverBookResponse naverResponse) {
        List<BookResponse> books = naverResponse.getItems().stream()
                .map(BookConverter::toBookResponse)
                .toList();

        int size = naverResponse.getDisplay();
        int page = naverResponse.getStart() / naverResponse.getDisplay() + 1;
        long totalElements = naverResponse.getTotal();
        int totalPages = (int) Math.ceil((double) totalElements / size);

        return new BookSearchResponse(
                books,
                page,
                size,
                totalElements,
                totalPages
        );
    }

    public static List<BookPopularResponse> toBookPopularResponse(List<Book> books) {
        return books.stream().map(book -> new BookPopularResponse(
                book.getImage(),
                book.getTitle(),
                book.getAuthor(),
                book.getPublisher(),
                book.getIsbn())).toList();
    }
}
