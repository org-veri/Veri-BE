package org.goorm.veri.veribe.domain.book.dto.reading;

import org.goorm.veri.veribe.domain.book.dto.reading.response.ReadingDetailResponse;
import org.goorm.veri.veribe.domain.book.entity.Reading;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;

import java.util.List;

public class ReadingConverter {

    public static ReadingDetailResponse toReadingDetailResponse(Reading reading) {

        List<ReadingDetailResponse.CardSummaryResponse> summaries = reading.getCards().stream()
                .map(card -> new ReadingDetailResponse.CardSummaryResponse(card.getId(), card.getImage()))
                .toList();

        return ReadingDetailResponse.builder()
                .memberBookId(reading.getId())
                .member(MemberProfile.from(reading.getMember()))
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
