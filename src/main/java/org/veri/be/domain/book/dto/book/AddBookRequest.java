package org.veri.be.domain.book.dto.book;

import jakarta.validation.constraints.NotNull;

public record AddBookRequest(
        @NotNull
        String title,
        @NotNull
        String image,
        @NotNull
        String author,
        @NotNull
        String publisher,
        @NotNull
        String isbn,
        Boolean isPublic
) {
    public AddBookRequest { // compact constructor
        if (isPublic == null) {
            isPublic = false;
        }
    }
}
