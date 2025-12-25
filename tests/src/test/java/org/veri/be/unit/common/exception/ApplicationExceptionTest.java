package org.veri.be.unit.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

class ApplicationExceptionTest {

    @Nested
    @DisplayName("http status")
    class HttpStatusMapping {

        @Test
        @DisplayName("HTTP 상태 코드가 매핑된다")
        void mapsHttpStatus() {
            assertStatus(ApplicationException.of(CardErrorInfo.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            assertStatus(ApplicationException.of(CardErrorInfo.FORBIDDEN), HttpStatus.FORBIDDEN);
            assertStatus(ApplicationException.of(CardErrorInfo.NOT_FOUND), HttpStatus.NOT_FOUND);
        }
    }

    private void assertStatus(ApplicationException exception, HttpStatus expected) {
        assertThat(exception.getStatusCode()).isEqualTo(expected);
        assertThat(exception.getErrorCode()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(exception.getErrorCode().getMessage());
    }
}
