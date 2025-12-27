package org.veri.be.domain.book.dto.book;

import java.util.List;

public record BookSearchResponse(
        List<BookResponse> books,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
    public static BookSearchResponse from(NaverBookResponse naverResponse) {
        List<BookResponse> books = naverResponse.getItems().stream()
                .map(BookResponse::from)
                .toList();

        int size = naverResponse.getDisplay();
        if (size <= 0) {
            size = 10;
        }

        int page = naverResponse.getStart() / size + 1;
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
}
