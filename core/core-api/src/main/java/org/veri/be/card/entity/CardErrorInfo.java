package org.veri.be.card.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.veri.be.lib.exception.ErrorCode;

@AllArgsConstructor
@Getter
public enum CardErrorInfo implements ErrorCode {
    IMAGE_TOO_LARGE(HttpStatus.BAD_REQUEST, "이미지 용량이 너무 큽니다.", "C1003"),
    UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "지원하지 않는 이미지 타입입니다.", "C1004"),
    READING_MS_NOT_PUBLIC(HttpStatus.FORBIDDEN, "독서 기록이 비공개 상태입니다.", "C1005")
    ;


    private final HttpStatus status;
    private final String message;
    private final String code;
}

