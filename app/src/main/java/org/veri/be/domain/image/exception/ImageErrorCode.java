package org.veri.be.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.veri.be.lib.exception.ErrorCode;

@AllArgsConstructor
@Getter
public enum ImageErrorCode implements ErrorCode {
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "잘못된 요청입니다.", "I1001"),
    OCR_PROCESSING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "OCR 처리에 실패했습니다.", "I1002");

    private final HttpStatus status;
    private final String message;
    private final String code;
}

