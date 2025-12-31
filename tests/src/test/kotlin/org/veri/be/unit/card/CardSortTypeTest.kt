package org.veri.be.unit.card

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.card.controller.enums.CardSortType
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions

class CardSortTypeTest {

    @Nested
    @DisplayName("from")
    inner class From {

        @Test
        @DisplayName("정렬 문자열을 타입으로 변환한다")
        fun convertsValue() {
            assertThat(CardSortType.from("newest")).isEqualTo(CardSortType.NEWEST)
            assertThat(CardSortType.from("oldest")).isEqualTo(CardSortType.OLDEST)
        }

        @Test
        @DisplayName("알 수 없는 값이면 예외가 발생한다")
        fun throwsWhenUnknown() {
            ExceptionAssertions.assertApplicationException(
                { CardSortType.from("invalid") },
                CommonErrorCode.INVALID_REQUEST
            )
        }
    }
}
