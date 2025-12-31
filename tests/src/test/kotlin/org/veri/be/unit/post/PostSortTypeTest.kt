package org.veri.be.unit.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.post.controller.enums.PostSortType
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions

class PostSortTypeTest {

    @Nested
    @DisplayName("from")
    inner class From {

        @Test
        @DisplayName("정렬 값이 newest면 NEWEST를 반환한다")
        fun returnsNewest() {
            val result = PostSortType.from("newest")

            assertThat(result).isEqualTo(PostSortType.NEWEST)
        }

        @Test
        @DisplayName("정렬 값이 oldest면 OLDEST를 반환한다")
        fun returnsOldest() {
            val result = PostSortType.from("oldest")

            assertThat(result).isEqualTo(PostSortType.OLDEST)
        }

        @Test
        @DisplayName("정렬 값이 다르면 예외가 발생한다")
        fun throwsWhenInvalid() {
            ExceptionAssertions.assertApplicationException(
                { PostSortType.from("invalid") },
                CommonErrorCode.INVALID_REQUEST
            )
        }
    }
}
