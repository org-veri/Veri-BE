package org.goorm.veri.veribe.domain.card.controller.dto.request;

public record CardCreateRequest(
        String content,
        String imageUrl,
        Long memberBookId,
        Boolean isPublic
) {

    public CardCreateRequest { // compact constructor
        if (isPublic == null) {
            isPublic = false;
        }
    }
}
