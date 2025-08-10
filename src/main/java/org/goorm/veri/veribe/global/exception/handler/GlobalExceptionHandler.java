package org.goorm.veri.veribe.global.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.goorm.veri.veribe.global.exception.ApplicationException;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.interceptors.InjectIPAddressInterceptor;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ApiResponse<Map<?, ?>> handleApplicationException(ApplicationException e) {
        if (e.getHttpStatus().is4xxClientError()) {
            log.warn(e.getMessage());
        } else {
            log.error(e.getMessage(), e);
        }

        return ApiResponse.error(e.getErrorInfo(), e.getHttpStatus());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Map<?, ?>> handleAnyCheckedException(Exception e) {
        log.error("Unexpected Error Occurred", e);
        return ApiResponse.error(CommonErrorInfo.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<?, ?>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.warn(e.getMessage());
        return ApiResponse.validationFailure(e.getFieldErrors());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Map<?, ?>> handleNoResourceFoundException(NoResourceFoundException e,
                                                                 HttpServletRequest request) {
        log.warn("Resource '{}' Not Found. Request IP : {}", request.getRequestURI(),
                request.getAttribute(InjectIPAddressInterceptor.IP));
        return ApiResponse.error(CommonErrorInfo.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Map<?, ?>> handleHttpRequestNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        log.warn("Method {} for '{}' Not supported. Request IP : {}", request.getMethod(),
                request.getRequestURI(),
                request.getAttribute(InjectIPAddressInterceptor.IP));
        return ApiResponse.error(CommonErrorInfo.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ApiResponse<Map<?, ?>> handleUnrecognizedPropertyException(
            UnrecognizedPropertyException e,
            HttpServletRequest request
    ) {
        log.error("Unrecognized property in request for {} : {}", request.getRequestURI(),
                e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Map<?, ?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.warn(e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Map<?, ?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.warn(e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }
}
