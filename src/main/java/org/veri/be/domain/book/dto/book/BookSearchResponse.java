package org.veri.be.domain.book.dto.book;

import java.util.List;

public record BookSearchResponse(
        List<BookResponse> books,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
