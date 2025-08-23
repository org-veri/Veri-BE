package org.goorm.veri.veribe.domain.book.dto.book;

public record AddBookRequest(
        String title,
        String image,
        String author,
        String publisher,
        String isbn,
        Boolean isPublic
) {
    public AddBookRequest { // compact constructor
        if (isPublic == null) {
            isPublic = false;
        }
    }
}
