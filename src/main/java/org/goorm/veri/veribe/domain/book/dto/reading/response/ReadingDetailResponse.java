package org.goorm.veri.veribe.domain.book.dto.reading.response;

import lombok.Builder;
import org.goorm.veri.veribe.domain.book.entity.enums.BookStatus;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReadingDetailResponse(
        Long memberBookId,
        MemberProfile member,
        String title,
        String author,
        String imageUrl,
        BookStatus status,
        Double score,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<CardSummaryResponse> cardSummaries
) {
    public record CardSummaryResponse(
            Long cardId,
            String cardImage,
            boolean isPublic
    ) {
    }
}
