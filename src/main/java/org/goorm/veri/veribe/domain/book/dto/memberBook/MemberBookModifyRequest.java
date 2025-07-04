package org.goorm.veri.veribe.domain.book.dto.memberBook;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record MemberBookModifyRequest(
        @Min(0)
        @Max(5)
        @Digits(integer = 1, fraction = 1)
        Double score,
        LocalDateTime startedAt,
        LocalDateTime endedAt
) {
    @AssertTrue(message = "0.5 단위로만 입력할 수 있습니다.")
    private boolean isHalfStep() {
        if (score == null) return true;

        BigDecimal bd = BigDecimal.valueOf(score);
        BigDecimal remainder = bd.remainder(new BigDecimal("0.5")).abs(); //Client 로부터 받은 수를 0.5 로 나눈 나머지

        return remainder.compareTo(BigDecimal.ZERO) == 0; //나머지가 0 이면 .5 단위 수 -> 정상
    }

}
