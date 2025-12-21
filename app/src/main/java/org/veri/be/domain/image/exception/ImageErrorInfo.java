package org.veri.be.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.lib.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum ImageErrorInfo implements ErrorInfo {
    BAD_REQUEST("잘못된 요청입니다.", "I1001"),
    OCR_PROCESSING_FAILED("OCR 처리에 실패했습니다.", "I1002");

    private final String message;
    private final String code;
}

