package org.veri.be.unit.book;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.controller.enums.ReadingSortType;
import org.veri.be.domain.book.exception.BookErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

class ReadingSortTypeTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("정렬 값이 newest면 NEWEST를 반환한다")
        void returnsNewest() {
            ReadingSortType result = ReadingSortType.from("newest");

            assertThat(result).isEqualTo(ReadingSortType.NEWEST);
        }

        @Test
        @DisplayName("정렬 값이 oldest면 OLDEST를 반환한다")
        void returnsOldest() {
            ReadingSortType result = ReadingSortType.from("oldest");

            assertThat(result).isEqualTo(ReadingSortType.OLDEST);
        }

        @Test
        @DisplayName("정렬 값이 score면 SCORE를 반환한다")
        void returnsScore() {
            ReadingSortType result = ReadingSortType.from("score");

            assertThat(result).isEqualTo(ReadingSortType.SCORE);
        }

        @Test
        @DisplayName("정렬 값이 다르면 예외가 발생한다")
        void throwsWhenInvalid() {
            ExceptionAssertions.assertApplicationException(
                    () -> ReadingSortType.from("invalid"),
                    BookErrorCode.BAD_REQUEST
            );
        }
    }
}
