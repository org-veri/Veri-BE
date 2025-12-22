package org.veri.be.unit.card;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.card.controller.enums.CardSortType;
import org.veri.be.domain.card.exception.CardErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

class CardSortTypeTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("정렬 문자열을 타입으로 변환한다")
        void convertsValue() {
            assertThat(CardSortType.from("newest")).isEqualTo(CardSortType.NEWEST);
            assertThat(CardSortType.from("oldest")).isEqualTo(CardSortType.OLDEST);
        }

        @Test
        @DisplayName("알 수 없는 값이면 예외가 발생한다")
        void throwsWhenUnknown() {
            ExceptionAssertions.assertApplicationException(
                    () -> CardSortType.from("invalid"),
                    CardErrorCode.BAD_REQUEST
            );
        }
    }
}
