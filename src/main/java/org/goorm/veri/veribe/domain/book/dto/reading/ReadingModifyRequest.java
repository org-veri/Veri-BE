package org.goorm.veri.veribe.domain.book.dto.reading;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record ReadingModifyRequest(
        @Min(0)
        @Max(5)
        @Digits(integer = 1, fraction = 1)
        Double score,

        @Past(message = "시작 시간은 과거 시간이어야 합니다.")
        LocalDateTime startedAt,

        @Past(message = "종료 시간은 과거 시간이어야 합니다.")
        LocalDateTime endedAt
) {
    @AssertTrue(message = "0.5 단위로만 입력할 수 있습니다.")
    private boolean isHalfStep() {
        if (score == null) return true;

        BigDecimal bd = BigDecimal.valueOf(score);
        BigDecimal remainder = bd.remainder(new BigDecimal("0.5")).abs(); //Client 로부터 받은 수를 0.5 로 나눈 나머지

        return remainder.compareTo(BigDecimal.ZERO) == 0; //나머지가 0 이면 .5 단위 수 -> 정상
    }

    @AssertTrue(message = "종료 시간은 시작 시간 이후여야 합니다.")
    private boolean isEndAfterStart() {
        if (startedAt == null || endedAt == null) return true;

        return !endedAt.isBefore(startedAt);
    }
}
