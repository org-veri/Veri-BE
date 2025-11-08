package org.veri.be.domain.auth.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.lib.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum AuthErrorInfo implements ErrorInfo {
    UNSUPPORTED_OAUTH2_PROVIDER("지원하지 않는 OAuth2 제공자입니다.", "A2001"),
    FAIL_GET_USER_INFO("사용자 정보를 가져오는 데 실패했습니다.", "A2002"),
    FAIL_GET_ACCESS_TOKEN("액세스 토큰을 가져오는 데 실패했습니다.", "A2003"),
    UNAUTHORIZED("인증되지 않은 사용자입니다.", "A2004"),
    FORBIDDEN("권한이 없는 사용자입니다.", "A2005"),
    FAIL_PROCESS_RESPONSE("인증중 응답을 처리하는 데 실패했습니다.", "A2006");

    private final String message;
    private final String code;
}

