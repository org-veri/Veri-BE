package org.veri.be.lib.auth.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.lib.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum TokenErrorInfo implements ErrorInfo {

    NOT_FOUND_ACCESS_TOKEN("엑세스 토큰을 찾을 수 없습니다", "A10001"),
    INVALID_TOKEN("잘못된 토큰입니다", "A10002"),
    NOT_FOUND_REFRESH_TOKEN("리프레시 토큰을 찾을 수 없습니다", "A10003"),
    CLAIMS_NOT_BE_NULL("JWT Claim은 null일 수 없습니다", "A10004"),
    EXPIRED_TOKEN("만료된 토큰입니다", "A10005"),
    NOT_FOUND_ALL_FIELDS_IN_ACCESS_TOKEN("토큰에 모든 정보가 존재하지 않습니다", "A10006");

    private final String message;
    private final String code;
}
