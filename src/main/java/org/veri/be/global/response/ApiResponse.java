package org.veri.be.global.response;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import org.veri.be.global.exception.CommonErrorInfo;
import org.veri.be.global.exception.ErrorInfo;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@JsonSerialize
@Builder
@Getter
@ToString
public class ApiResponse<T> {

    private static final String SUCCESS_CODE = "C0000";
    private static final String SUCCESS_MESSAGE = "요청에 성공했습니다.";

    @JsonIgnore
    private HttpStatus httpStatus;

    @JsonIgnore
    private final List<ApiHeader> headers = new ArrayList<>();

    @JsonIgnore
    private MediaType contentType;

    private Boolean isSuccess;
    private String code;
    private String message;
    private T result;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.ok(data, MediaType.APPLICATION_JSON);
    }

    public static <T> ApiResponse<T> ok(T data, MediaType contentType) {
        return ApiResponse.<T>builder()
                .httpStatus(HttpStatus.OK)
                .isSuccess(true)
                .result(data)
                .contentType(contentType)
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static <T> ApiResponse<T> created(T data) {
        return ApiResponse.created(data, MediaType.APPLICATION_JSON);
    }

    public static <T> ApiResponse<T> created(T data, MediaType contentType) {
        return ApiResponse.<T>builder()
                .httpStatus(HttpStatus.CREATED)
                .isSuccess(true)
                .result(data)
                .contentType(contentType)
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static ApiResponse<Void> noContent() {
        return ApiResponse.<Void>builder()
                .httpStatus(HttpStatus.NO_CONTENT)
                .isSuccess(true)
                .result(null)
                .contentType(MediaType.APPLICATION_JSON)
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static <T> ApiResponse<T> from(HttpStatus httpStatus, T data) {
        return ApiResponse.from(httpStatus, data, MediaType.APPLICATION_JSON);
    }

    public static <T> ApiResponse<T> from(HttpStatus httpStatus, T data, MediaType contentType) {
        return ApiResponse.<T>builder()
                .httpStatus(httpStatus)
                .isSuccess(true)
                .result(data)
                .contentType(contentType)
                .code(SUCCESS_CODE)
                .message(SUCCESS_MESSAGE)
                .build();
    }

    public static ApiResponse<Map<?, ?>> error(ErrorInfo errorInfo, HttpStatus httpStatus) {
        return ApiResponse.error(errorInfo, httpStatus, MediaType.APPLICATION_JSON);
    }

    public static ApiResponse<Map<?, ?>> error(
            ErrorInfo errorInfo,
            HttpStatus httpStatus,
            MediaType contentType
    ) {
        return ApiResponse.<Map<?, ?>>builder()
                .httpStatus(httpStatus)
                .isSuccess(false)
                .result(new HashMap<>())
                .code(errorInfo.getCode())
                .message(errorInfo.getMessage())
                .contentType(contentType)
                .build();
    }

    public static ResponseEntity<ApiResponse<Map<?, ?>>> validationFailure(List<FieldError> fieldErrors) {
        return ApiResponse.validationFailure(fieldErrors, MediaType.APPLICATION_JSON);
    }

    public static ResponseEntity<ApiResponse<Map<?, ?>>> validationFailure(
            List<FieldError> fieldErrors,
            MediaType contentType
    ) {
        Map<String, String> errors = new HashMap<>();
        fieldErrors.forEach(
                fieldError -> errors.put(fieldError.getField(), fieldError.getDefaultMessage()));

        ApiResponse<Map<?, ?>> apiResponse = ApiResponse.<Map<?, ?>>builder()
                .isSuccess(false)
                .result(errors)
                .message(CommonErrorInfo.NOT_VALID_REQUEST_FIELDS.getMessage())
                .contentType(contentType)
                .httpStatus(HttpStatus.BAD_REQUEST)
                .code(CommonErrorInfo.NOT_VALID_REQUEST_FIELDS.getCode())
                .build();
        return ResponseEntity.ok(apiResponse);
    }
}
