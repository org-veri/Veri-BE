package org.veri.be.domain.book.repository.dto;

import org.veri.be.domain.book.entity.enums.ReadingStatus;

import java.time.LocalDateTime;

public record ReadingQueryResult(
        Long bookId,
        Long memberBookId,
        String title,
        String author,
        String imageUrl,
        Double score,
        LocalDateTime startedAt,
        ReadingStatus status,
        boolean isPublic
) {
}
