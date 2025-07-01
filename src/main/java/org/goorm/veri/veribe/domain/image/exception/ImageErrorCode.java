package org.goorm.veri.veribe.domain.image.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.namul.api.payload.code.BaseErrorCode;
import org.namul.api.payload.code.dto.ErrorReasonDTO;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.springframework.http.HttpStatus;

@Getter
@AllArgsConstructor
public enum ImageErrorCode implements BaseErrorCode {
    NOT_FOUND(HttpStatus.NOT_FOUND, "IMG404", "해당 이미지를 찾을 수 없습니다."),
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "IMG400", "잘못된 요청입니다. 요청을 확인해주세요."),
    FORBIDDEN(HttpStatus.FORBIDDEN, "IMG401", "해당 이미지에 대한 권한이 없습니다."),
    CROP_RANGE_INVALID(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_1", "크롭된 이미지의 좌표 및 높낮이 값이 유효하지 않습니다."),
    SIZE_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_2", "이미지 크기는 1MB를 초과할 수 없습니다."),
    UNSUPPORTED_TYPE(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_3", "지원하지 않는 파일 형식입니다.");

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
