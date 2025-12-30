package org.veri.be.unit.common.response

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageRequest
import org.veri.be.global.response.PageResponse

class PageResponseTest {

    @Nested
    @DisplayName("of")
    inner class Of {

        @Test
        @DisplayName("0-based 페이지를 1-based로 보정한다")
        fun adjustsPageIndex() {
            val response = PageResponse.of(
                listOf("a", "b"),
                0,
                10,
                2,
                1
            )

            assertThat(response.page()).isEqualTo(1)
            assertThat(response.size()).isEqualTo(10)
            assertThat(response.totalElements()).isEqualTo(2)
            assertThat(response.totalPages()).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("empty")
    inner class Empty {

        @Test
        @DisplayName("빈 응답을 생성한다")
        fun returnsEmptyResponse() {
            val pageable = PageRequest.of(2, 5)

            val response = PageResponse.empty<List<String>>(pageable)

            assertThat(response.content()).isNull()
            assertThat(response.page()).isEqualTo(3)
            assertThat(response.size()).isEqualTo(5)
            assertThat(response.totalElements()).isZero()
            assertThat(response.totalPages()).isZero()
        }
    }
}
