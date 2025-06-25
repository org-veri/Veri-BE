package org.goorm.veri.veribe.domain.book.entity.dtos.book;

public class BookConverter {

    public static BookResponse toBookResponse(NaverBookItem response) {
        return BookResponse.builder()
                .author(response.getAuthor())
                .imageUrl(response.getImage())
                .title(response.getTitle())
                .build();
    }
}
