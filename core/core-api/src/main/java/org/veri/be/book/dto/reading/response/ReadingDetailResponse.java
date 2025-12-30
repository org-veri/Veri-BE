package org.veri.be.book.dto.reading.response;

import lombok.Builder;
import org.veri.be.book.entity.enums.ReadingStatus;
import org.veri.be.book.entity.Reading;
import org.veri.be.member.entity.Member;
import org.veri.be.member.dto.MemberProfileResponse;

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
        List<ReadingCardSummaryResponse> cardSummaries,
        boolean isPublic
) {
    public static ReadingDetailResponse from(
            Reading reading,
            Member viewer,
            List<ReadingCardSummaryResponse> summaries
    ) {
        boolean isOwner = viewer != null && reading.getMember().getId().equals(viewer.getId());
        List<ReadingCardSummaryResponse> filteredSummaries = isOwner
                ? summaries
                : summaries.stream().filter(ReadingCardSummaryResponse::isPublic).toList();

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
                .cardSummaries(filteredSummaries)
                .isPublic(reading.isPublic())
                .build();
    }
}
