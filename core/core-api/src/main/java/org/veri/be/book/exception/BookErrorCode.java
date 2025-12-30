package org.veri.be.book.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.veri.be.lib.exception.ErrorCode;

@AllArgsConstructor
@Getter
public enum BookErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "B1001"),
    NAVER_API_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "네이버 API 오류입니다.", "B1002"),
    ALREADY_EXIST(HttpStatus.CONFLICT, "이미 등록된 책입니다.", "B1003");

    private final HttpStatus status;
    private final String message;
    private final String code;
}

