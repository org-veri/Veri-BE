package org.goorm.veri.veribe.domain.book.dto.memberBook;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Digits;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import java.math.BigDecimal;

public record MemberBookScoreRequest (
        @Min(0)
        @Max(5) //최소 0점 ~ 최고 5점
        @Digits(integer = 1, fraction = 0) //소수점 단위 수는 받을 수 없도록 제한
        Double score
) {

    //추후 기획 변경을 통해 0.5 단위로 점수 받을 시, 활성화 & fraction = 1 로 수정
    /*
    @AssertTrue(message = "0.5 단위로만 입력할 수 있습니다.")
    private boolean isHalfStep() {
        if (score == null) return true;

        BigDecimal bd = BigDecimal.valueOf(score);
        BigDecimal remainder = bd.remainder(new BigDecimal("0.5")).abs(); //Client 로부터 받은 수를 0.5 로 나눈 나머지

        return remainder.compareTo(BigDecimal.ZERO) == 0; //나머지가 0 이면 .5 단위 수 -> 정상
    }
    */
}
