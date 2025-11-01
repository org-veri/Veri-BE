package org.veri.be.domain.book.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.global.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum BookErrorInfo implements ErrorInfo {
    BAD_REQUEST("잘못된 요청입니다.", "B1001"),
    NAVER_API_ERROR("네이버 API 오류입니다.", "B1002"),
    ALREADY_EXIST("이미 등록된 책입니다.", "B1003");

    private final String message;
    private final String code;
}

