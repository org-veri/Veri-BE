package org.veri.be.lib.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(ApplicationException.class)
    public ApiResponse<Map<?, ?>> handleApplicationException(ApplicationException e) {
        if (e.getHttpStatus().is4xxClientError()) {
            log.info(e.getMessage());
        } else {
            log.warn(e.getMessage(), e);
        }

        return ApiResponse.error(e.getErrorInfo(), e.getHttpStatus());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Map<?, ?>> handleAnyUnexpectedException(Exception e) {
        log.warn("Unexpected Error Occurred", e);
        return ApiResponse.error(CommonErrorInfo.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Map<?, ?>> handleNoResourceFoundException(NoResourceFoundException e) {
        return ApiResponse.error(CommonErrorInfo.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<Map<?, ?>> handleNoHandlerFoundException(NoHandlerFoundException e) {
        return ApiResponse.error(CommonErrorInfo.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Map<?, ?>> handleHttpRequestNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        log.debug("Method {} for '{}' Not supported. Request IP : {}", request.getMethod(),
                request.getRequestURI(),
                request.getAttribute(InjectIPAddressInterceptor.IP));
        return ApiResponse.error(CommonErrorInfo.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<?, ?>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e) {
        log.debug(e.getMessage());
        return ApiResponse.validationFailure(e.getFieldErrors());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Map<?, ?>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e) {
        log.debug(e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Map<?, ?>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e) {
        log.debug(e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiResponse<Map<?, ?>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e) {
        log.debug(e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }
    
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ApiResponse<Map<?, ?>> handleUnrecognizedPropertyException(
            UnrecognizedPropertyException e,
            HttpServletRequest request
    ) {
        log.debug("Unrecognized property in request for {} : {}", request.getRequestURI(),
                e.getMessage());
        return ApiResponse.error(CommonErrorInfo.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }
}
