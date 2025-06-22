package org.goorm.veri.veribe.domain.card.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.code.dto.ErrorReasonDTO;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum CardErrorCode implements BaseErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "CARD404", "카드를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "CARD400", "잘못된 요청입니다. 요청을 확인해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "CARD401", "해당 카드에 대한 권한이 없습니다."),

    UNSUPPORTED_IMAGE_TYPE(HttpStatus.BAD_REQUEST, "CARD4001", "지원하지 않는 파일 형식입니다. 이미지만 업로드할 수 있습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

    @Override
    public ErrorReasonDTO getReason() {
        return DefaultResponseErrorReasonDTO.builder()
                .httpStatus(this.httpStatus)
                .code(this.code)
                .message(this.message)
                .build();
    }
}
