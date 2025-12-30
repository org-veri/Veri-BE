package org.veri.be.book.dto.reading.response;

public record ReadingVisibilityUpdateResponse(
        Long id,
        Boolean isPublic
) {
}
