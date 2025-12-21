package org.veri.be.unit.common.response;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageRequest;
import org.veri.be.global.response.PageResponse;

class PageResponseTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("0-based 페이지를 1-based로 보정한다")
        void adjustsPageIndex() {
            PageResponse<List<String>> response = PageResponse.of(
                    List.of("a", "b"),
                    0,
                    10,
                    2,
                    1
            );

            assertThat(response.page()).isEqualTo(1);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(2);
            assertThat(response.totalPages()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("empty")
    class Empty {

        @Test
        @DisplayName("빈 응답을 생성한다")
        void returnsEmptyResponse() {
            PageRequest pageable = PageRequest.of(2, 5);

            PageResponse<List<String>> response = PageResponse.empty(pageable);

            assertThat(response.content()).isNull();
            assertThat(response.page()).isEqualTo(3);
            assertThat(response.size()).isEqualTo(5);
            assertThat(response.totalElements()).isZero();
            assertThat(response.totalPages()).isZero();
        }
    }
}
