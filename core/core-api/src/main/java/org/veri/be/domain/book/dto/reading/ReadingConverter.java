package org.veri.be.domain.book.dto.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.member.entity.Member;
import org.veri.be.global.auth.context.CurrentMemberAccessor;

@Component
@RequiredArgsConstructor
public class ReadingConverter {

    private final CurrentMemberAccessor currentMemberAccessor;

    public ReadingDetailResponse toReadingDetailResponse(Reading reading) {
        Member viewer = currentMemberAccessor.getCurrentMember().orElse(null);
        return ReadingDetailResponse.from(reading, viewer);
    }
}
