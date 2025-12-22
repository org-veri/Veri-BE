package org.veri.be.unit.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.veri.be.domain.card.exception.CardErrorCode;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

class ApplicationExceptionTest {

    @Nested
    @DisplayName("http status")
    class HttpStatusMapping {

        @Test
        @DisplayName("에러 코드의 HTTP 상태가 그대로 노출된다")
        void mapsHttpStatus() {
            assertStatus(ApplicationException.of(CardErrorCode.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            assertStatus(ApplicationException.of(CardErrorCode.NOT_FOUND), HttpStatus.NOT_FOUND);
            assertStatus(ApplicationException.of(CommonErrorCode.INTERNAL_SERVER_ERROR), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void assertStatus(ApplicationException exception, HttpStatus expected) {
        assertThat(exception.getStatusCode()).isEqualTo(expected);
        assertThat(exception.getErrorCode()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(exception.getErrorCode().getMessage());
    }
}
