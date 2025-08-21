package org.goorm.veri.veribe.domain.book.dto.reading;

import org.goorm.veri.veribe.domain.book.entity.Reading;

import java.util.List;

public class ReadingConverter {

    public static ReadingDetailResponse toReadingDetailResponse(Reading reading) {

        List<CardSummaries> summaries = reading.getCards().stream()
                                        .map(card -> new CardSummaries(card.getId(), card.getImage()))
                                        .toList();

        return ReadingDetailResponse.builder()
                .memberBookId(reading.getId())
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
