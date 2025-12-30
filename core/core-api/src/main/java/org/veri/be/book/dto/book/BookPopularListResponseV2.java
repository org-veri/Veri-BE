package org.veri.be.book.dto.book;

import java.util.List;

public record BookPopularListResponseV2(
        List<BookPopularResponse> books
) {
}
