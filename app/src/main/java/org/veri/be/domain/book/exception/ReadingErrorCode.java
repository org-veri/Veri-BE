package org.veri.be.domain.book.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.veri.be.lib.exception.ErrorCode;

@AllArgsConstructor
@Getter
public enum ReadingErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "MB1001"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "권한이 없습니다.", "MB1002"),
    ALREADY_EXIST(HttpStatus.CONFLICT, "이미 등록된 책입니다.", "MB1003");

    private final HttpStatus status;
    private final String message;
    private final String code;
}

