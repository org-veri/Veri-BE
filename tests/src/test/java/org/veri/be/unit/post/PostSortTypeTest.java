package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.card.exception.CardErrorCode;
import org.veri.be.domain.post.controller.enums.PostSortType;
import org.veri.be.support.assertion.ExceptionAssertions;

class PostSortTypeTest {

    @Nested
    @DisplayName("from")
    class From {

        @Test
        @DisplayName("정렬 값이 newest면 NEWEST를 반환한다")
        void returnsNewest() {
            PostSortType result = PostSortType.from("newest");

            assertThat(result).isEqualTo(PostSortType.NEWEST);
        }

        @Test
        @DisplayName("정렬 값이 oldest면 OLDEST를 반환한다")
        void returnsOldest() {
            PostSortType result = PostSortType.from("oldest");

            assertThat(result).isEqualTo(PostSortType.OLDEST);
        }

        @Test
        @DisplayName("정렬 값이 다르면 예외가 발생한다")
        void throwsWhenInvalid() {
            ExceptionAssertions.assertApplicationException(
                    () -> PostSortType.from("invalid"),
                    CardErrorCode.BAD_REQUEST
            );
        }
    }
}
