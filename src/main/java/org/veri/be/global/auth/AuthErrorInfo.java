package org.veri.be.global.auth;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.lib.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum AuthErrorInfo implements ErrorInfo {
    UNSUPPORTED_OAUTH2_PROVIDER("지원하지 않는 OAuth2 제공자입니다.", "A2001"),
    UNAUTHORIZED("인증되지 않은 사용자입니다.", "A2004"),
    FORBIDDEN("권한이 없는 사용자입니다.", "A2005");

    private final String message;
    private final String code;
}

