package org.veri.be.book.event;

public record ReadingVisibilityChangedEvent(
        Long readingId,
        boolean isPublic
) {
}
