package org.veri.be.domain.book.dto.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.context.CurrentMemberAccessor;

import java.util.List;

@Component
@RequiredArgsConstructor
public class ReadingConverter {

    private final CurrentMemberAccessor currentMemberAccessor;

    public ReadingDetailResponse toReadingDetailResponse(Reading reading) {
        List<ReadingDetailResponse.CardSummaryResponse> summaries = reading.getCards().stream()
                .map(card -> new ReadingDetailResponse.CardSummaryResponse(card.getId(), card.getImage(), card.getIsPublic()))
                .toList();

        Member viewer = currentMemberAccessor.getCurrentMember().orElse(null);
        boolean isOwner = viewer != null && reading.getMember().getId().equals(viewer.getId());

        if (!isOwner) {
            summaries = summaries.stream()
                    .filter(ReadingDetailResponse.CardSummaryResponse::isPublic)
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
                .build();
    }
}
