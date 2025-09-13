package org.goorm.veri.veribe.domain.book.dto.reading.response;

import lombok.Builder;

import org.goorm.veri.veribe.domain.book.entity.enums.ReadingStatus;

import java.time.LocalDateTime;

@Builder
public record ReadingResponse (
        Long memberBookId,
        String title,
        String author,
        String imageUrl,
        Double score,
        LocalDateTime startedAt,
        ReadingStatus status,
        boolean isPublic
)
{}

