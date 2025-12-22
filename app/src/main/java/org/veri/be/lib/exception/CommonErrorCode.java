package org.veri.be.lib.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum CommonErrorCode implements ErrorCode {

    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "요청 값이 잘못되었거나 비어있습니다.", "V7777"),
    NOT_VALID_REQUEST_FIELDS(HttpStatus.BAD_REQUEST, "요청 필드 검증에 실패했습니다.", "V8888"),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "서버에 오류가 발생했습니다. 잠시후 다시 시도해주세요.", "E9999"),

    NOT_FOUND_TOKEN_MEMBER_CONTEXT(HttpStatus.UNAUTHORIZED, "인증정보를 찾을 수 없습니다.", "A10008"),
    DOES_NOT_HAVE_PERMISSION(HttpStatus.FORBIDDEN, "이 작업을 수행할 권한이 없습니다", "A10007"),

    RESOURCE_NOT_FOUND(HttpStatus.NOT_FOUND, "요청한 리소스를 찾을 수 없습니다", "10010"),

    ;

    private final HttpStatus status;
    private final String message;
    private final String code;
}
