package org.veri.be.domain.card.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.veri.be.global.exception.ErrorInfo;

@AllArgsConstructor
@Getter
public enum CardErrorInfo implements ErrorInfo {
    NOT_FOUND("카드를 찾을 수 없습니다.", "C1000"),
    BAD_REQUEST("잘못된 요청입니다.", "C1001"),
    FORBIDDEN("권한이 없습니다.", "C1002"),
    IMAGE_TOO_LARGE("이미지 용량이 너무 큽니다.", "C1003"),
    UNSUPPORTED_IMAGE_TYPE("지원하지 않는 이미지 타입입니다.", "C1004"),
    READING_MS_NOT_PUBLIC("독서 기록이 비공개 상태입니다.", "C1005");

    private final String message;
    private final String code;
}

