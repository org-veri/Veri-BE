package org.veri.be.domain.book.dto.reading;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.veri.be.domain.book.dto.reading.response.ReadingDetailResponse;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.global.auth.context.CurrentMemberAccessor;
import org.veri.be.global.auth.context.CurrentMemberInfo;

@Component
@RequiredArgsConstructor
public class ReadingConverter {

    private final CurrentMemberAccessor currentMemberAccessor;

    public ReadingDetailResponse toReadingDetailResponse(Reading reading) {
        CurrentMemberInfo viewer = currentMemberAccessor.getCurrentMemberInfo().orElse(null);
        return ReadingDetailResponse.from(reading, viewer);
    }
}
