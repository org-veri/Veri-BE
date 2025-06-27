package org.goorm.veri.veribe.domain.book.dto.book;

import java.util.List;

public class BookConverter {

    public static BookResponse toBookResponse(NaverBookItem response) {
        return BookResponse.builder()
                .author(response.getAuthor())
                .imageUrl(response.getImage())
                .title(response.getTitle())
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
}
