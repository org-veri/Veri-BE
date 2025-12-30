package org.veri.be.card.controller.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CardCreateRequest(
        @NotBlank
        String content,
        String imageUrl,
        @NotNull
        Long memberBookId,
        Boolean isPublic
) {

    public CardCreateRequest { // compact constructor
        if (isPublic == null) {
            isPublic = false;
        }
    }
}
