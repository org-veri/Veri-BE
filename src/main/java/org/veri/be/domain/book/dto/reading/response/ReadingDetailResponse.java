package org.veri.be.domain.book.dto.reading.response;

import lombok.Builder;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.api.common.dto.MemberProfileResponse;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record ReadingDetailResponse(
        Long memberBookId,
        MemberProfileResponse member,
        String title,
        String author,
        String imageUrl,
        ReadingStatus status,
        Double score,
        LocalDateTime startedAt,
        LocalDateTime endedAt,
        List<CardSummaryResponse> cardSummaries,
        boolean isPublic
) {
    public record CardSummaryResponse(
            Long cardId,
            String cardImage,
            boolean isPublic
    ) {
    }
}
