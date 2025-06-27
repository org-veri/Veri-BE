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
}
