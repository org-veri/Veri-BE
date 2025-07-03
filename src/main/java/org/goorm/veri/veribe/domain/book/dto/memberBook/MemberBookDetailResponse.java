package org.goorm.veri.veribe.domain.book.dto.memberBook;

import lombok.Builder;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record MemberBookDetailResponse (
        Long memberBookId,
        String title,
        String author,
        String imageUrl,
        BookStatus status,
        Double score,
        LocalDateTime startedAt,
        List<CardSummaries> cardSummaries)
{}
