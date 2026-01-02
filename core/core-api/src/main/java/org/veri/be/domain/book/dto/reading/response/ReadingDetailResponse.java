package org.veri.be.domain.book.dto.reading.response;

import lombok.Builder;
import org.veri.be.domain.book.entity.enums.ReadingStatus;
import org.veri.be.api.common.dto.MemberProfileResponse;

import org.veri.be.domain.book.entity.Reading;
import org.veri.be.global.auth.context.CurrentMemberInfo;

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
    public static ReadingDetailResponse from(Reading reading, CurrentMemberInfo viewer) {
        List<CardSummaryResponse> summaries = reading.getCards().stream()
                .map(card -> new CardSummaryResponse(card.getId(), card.getImage(), card.isPublic()))
                .toList();

        boolean isOwner = viewer != null && reading.getMember().getId().equals(viewer.id());

        if (!isOwner) {
            summaries = summaries.stream()
                    .filter(CardSummaryResponse::isPublic)
                    .toList();
        }

        return ReadingDetailResponse.builder()
                .memberBookId(reading.getId())
                .member(MemberProfileResponse.from(reading.getMember()))
                .title(reading.getBook().getTitle())
                .imageUrl(reading.getBook().getImage())
                .status(reading.getStatus())
                .score(reading.getScore())
                .author(reading.getBook().getAuthor())
                .startedAt(reading.getStartedAt())
                .endedAt(reading.getEndedAt())
                .cardSummaries(summaries)
                .isPublic(reading.isPublic())
                .build();
    }

    public record CardSummaryResponse(
            Long cardId,
            String cardImage,
            boolean isPublic
    ) {
    }
}
