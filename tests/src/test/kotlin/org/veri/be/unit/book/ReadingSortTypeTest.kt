package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.book.controller.enums.ReadingSortType
import org.veri.be.book.exception.BookErrorCode
import org.veri.be.support.assertion.ExceptionAssertions

class ReadingSortTypeTest {

    @Nested
    @DisplayName("from")
    inner class From {

        @Test
        @DisplayName("정렬 값이 newest면 NEWEST를 반환한다")
        fun returnsNewest() {
            val result = ReadingSortType.from("newest")

            assertThat(result).isEqualTo(ReadingSortType.NEWEST)
        }

        @Test
        @DisplayName("정렬 값이 oldest면 OLDEST를 반환한다")
        fun returnsOldest() {
            val result = ReadingSortType.from("oldest")

            assertThat(result).isEqualTo(ReadingSortType.OLDEST)
        }

        @Test
        @DisplayName("정렬 값이 score면 SCORE를 반환한다")
        fun returnsScore() {
            val result = ReadingSortType.from("score")

            assertThat(result).isEqualTo(ReadingSortType.SCORE)
        }

        @Test
        @DisplayName("정렬 값이 다르면 예외가 발생한다")
        fun throwsWhenInvalid() {
            ExceptionAssertions.assertApplicationException(
                { ReadingSortType.from("invalid") },
                BookErrorCode.BAD_REQUEST
            )
        }
    }
}
