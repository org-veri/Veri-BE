package org.goorm.veri.veribe.domain.book.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.goorm.veri.veribe.global.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum ReadingErrorInfo implements ErrorInfo {
    BAD_REQUEST("잘못된 요청입니다.", "MB1001"),
    ALREADY_EXIST("이미 등록된 책입니다.", "MB1002");

    private final String message;
    private final String code;
}

