package org.veri.be.global.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.veri.be.lib.exception.ErrorCode;

@AllArgsConstructor
@Getter
public enum AuthErrorCode implements ErrorCode {
    UNSUPPORTED_OAUTH2_PROVIDER(HttpStatus.BAD_REQUEST, "지원하지 않는 OAuth2 제공자입니다.", "A2001"),
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다.", "A2004"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없는 사용자입니다.", "A2005");

    private final HttpStatus status;
    private final String message;
    private final String code;
}
