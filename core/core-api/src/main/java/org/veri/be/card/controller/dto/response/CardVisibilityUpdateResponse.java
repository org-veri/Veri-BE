package org.veri.be.card.controller.dto.response;

public record CardVisibilityUpdateResponse(
        Long id,
        Boolean isPublic
) {
}
