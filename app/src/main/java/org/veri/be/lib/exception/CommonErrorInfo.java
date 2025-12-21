package org.veri.be.lib.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum CommonErrorInfo implements ErrorInfo {

    // Request Body가 비어있거나, 잘못된 json key로 요청을 보낸 경우
    INVALID_REQUEST("요청 값이 잘못되었거나 비어있습니다.", "V7777"),

    // Request DTO 필드 검증에 실패했을 경우
    NOT_VALID_REQUEST_FIELDS("요청 필드 검증에 실패했습니다.", "V8888"),

    // 예상하지 못한 예외 또는 로직 상의 문제가 발생했을 경우 던지는 예외입니다
    INTERNAL_SERVER_ERROR("서버에 오류가 발생했습니다. 잠시후 다시 시도해주세요.", "E9999"),

    // auth
    DOES_NOT_HAVE_PERMISSION("이 작업을 수행할 권한이 없습니다", "A10007"),

    // member context
    NOT_FOUND_TOKEN_MEMBER_CONTEXT("TokenMemberContext가 설정되지 않았습니다.", "A10008"),

    RESOURCE_NOT_FOUND("요청한 리소스를 찾을 수 없습니다", "10010");

    private final String message;
    private final String code;
}
