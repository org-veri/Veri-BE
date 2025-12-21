package org.veri.be.unit.common.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.http.BadRequestException;
import org.veri.be.lib.exception.http.ConflictException;
import org.veri.be.lib.exception.http.ForbiddenException;
import org.veri.be.lib.exception.http.InternalServerException;
import org.veri.be.lib.exception.http.NotFoundException;
import org.veri.be.lib.exception.http.RequestTimeoutException;
import org.veri.be.lib.exception.http.TooManyRequestsException;
import org.veri.be.lib.exception.http.UnAuthorizedException;

class ApplicationExceptionTest {

    @Nested
    @DisplayName("http status")
    class HttpStatusMapping {

        @Test
        @DisplayName("HTTP 상태 코드가 매핑된다")
        void mapsHttpStatus() {
            assertStatus(new BadRequestException(CardErrorInfo.BAD_REQUEST), HttpStatus.BAD_REQUEST);
            assertStatus(new UnAuthorizedException(CardErrorInfo.FORBIDDEN), HttpStatus.UNAUTHORIZED);
            assertStatus(new ForbiddenException(CardErrorInfo.FORBIDDEN), HttpStatus.FORBIDDEN);
            assertStatus(new NotFoundException(CardErrorInfo.NOT_FOUND), HttpStatus.NOT_FOUND);
            assertStatus(new ConflictException(CardErrorInfo.BAD_REQUEST), HttpStatus.CONFLICT);
            assertStatus(new RequestTimeoutException(CardErrorInfo.BAD_REQUEST), HttpStatus.REQUEST_TIMEOUT);
            assertStatus(new TooManyRequestsException(CardErrorInfo.BAD_REQUEST), HttpStatus.TOO_MANY_REQUESTS);
            assertStatus(new InternalServerException(CardErrorInfo.BAD_REQUEST), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    private void assertStatus(ApplicationException exception, HttpStatus expected) {
        assertThat(exception.getHttpStatus()).isEqualTo(expected);
        assertThat(exception.getErrorInfo()).isNotNull();
        assertThat(exception.getMessage()).isEqualTo(exception.getErrorInfo().getMessage());
    }
}
