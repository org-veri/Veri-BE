package org.veri.be.book.dto.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.book.entity.Reading;
import org.veri.be.book.service.ReadingCardSummaryProvider;
import org.veri.be.member.entity.Member;
import org.veri.be.member.auth.context.CurrentMemberAccessor;

@Component
@RequiredArgsConstructor
public class ReadingConverter {

    private final CurrentMemberAccessor currentMemberAccessor;
    private final ReadingCardSummaryProvider readingCardSummaryProvider;

    public ReadingDetailResponse toReadingDetailResponse(Reading reading) {
        Member viewer = currentMemberAccessor.getCurrentMember().orElse(null);
        return ReadingDetailResponse.from(
                reading,
                viewer,
                readingCardSummaryProvider.getCardSummaries(reading.getId())
        );
    }
}
