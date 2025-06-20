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
    ENCODING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_1", "이미지 인코딩에 실패했습니다."),
    SIZE_EXCEEDED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_2", "이미지 크기는 1MB를 초과할 수 없습니다."),
    CONVERT_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "IMG500_3", "파일 변환 도중 오류가 발생했습니다.");

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
