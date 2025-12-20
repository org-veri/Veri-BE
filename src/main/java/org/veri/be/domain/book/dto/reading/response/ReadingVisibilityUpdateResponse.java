package org.veri.be.domain.book.dto.reading.response;

public record ReadingVisibilityUpdateResponse(
        Long id,
        Boolean isPublic
) {
}
