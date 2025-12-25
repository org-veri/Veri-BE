package org.veri.be.lib.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import java.net.URI;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import jakarta.validation.ConstraintViolationException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ExceptionLogger exceptionLogger = new ExceptionLogger();
    private final ValidationErrorMapper validationErrorMapper = new ValidationErrorMapper();

    @ExceptionHandler(ApplicationException.class)
    public ApiResponse<Map<String, Object>> handleApplicationException(
            ApplicationException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                e.getErrorCode().getStatus(),
                e.getMessage(),
                URI.create(request.getRequestURI()),
                e,
                "APPLICATION_EXCEPTION"
        );

        return ApiResponse.error(e.getErrorCode(), e.getErrorCode().getStatus());
    }

    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Map<String, Object>> handleAnyUnexpectedException(
            Exception e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error Occurred",
                URI.create(request.getRequestURI()),
                e,
                "UNEXPECTED_ERROR"
        );

        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoResourceFoundException.class)
    public ApiResponse<Map<String, Object>> handleNoResourceFoundException(
            NoResourceFoundException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.NOT_FOUND,
                CommonErrorCode.RESOURCE_NOT_FOUND.getMessage(),
                URI.create(request.getRequestURI()),
                e,
                "RESOURCE_NOT_FOUND"
        );

        return ApiResponse.error(CommonErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoHandlerFoundException.class)
    public ApiResponse<Map<String, Object>> handleNoHandlerFoundException(
            NoHandlerFoundException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.NOT_FOUND,
                CommonErrorCode.RESOURCE_NOT_FOUND.getMessage(),
                URI.create(request.getRequestURI()),
                e,
                "HANDLER_NOT_FOUND"
        );

        return ApiResponse.error(CommonErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ApiResponse<Map<String, Object>> handleHttpRequestNotSupportedException(
            HttpRequestMethodNotSupportedException e,
            HttpServletRequest request) {
        String message = String.format("Method %s for '%s' Not supported. Request IP: %s",
                request.getMethod(),
                request.getRequestURI(),
                request.getAttribute(InjectIPAddressInterceptor.IP));

        exceptionLogger.log(
                HttpStatus.NOT_FOUND,
                message,
                URI.create(request.getRequestURI()),
                e,
                "METHOD_NOT_SUPPORTED"
        );

        return ApiResponse.error(CommonErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request fields",
                URI.create(request.getRequestURI()),
                e,
                "VALIDATION_ERROR"
        );

        return ApiResponse.validationFailure(e.getFieldErrors());
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ApiResponse<Map<String, Object>> handleMethodArgumentTypeMismatchException(
            MethodArgumentTypeMismatchException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                CommonErrorCode.INVALID_REQUEST.getMessage(),
                URI.create(request.getRequestURI()),
                e,
                "TYPE_MISMATCH"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ApiResponse<Map<String, Object>> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Request message not readable",
                URI.create(request.getRequestURI()),
                e,
                "MESSAGE_NOT_READABLE"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiResponse<Map<String, Object>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Method parameter validation failed",
                URI.create(request.getRequestURI()),
                e,
                "METHOD_VALIDATION_ERROR"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Constraint violation occurred",
                URI.create(request.getRequestURI()),
                e,
                "CONSTRAINT_VIOLATION"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ApiResponse<Map<String, Object>> handleUnrecognizedPropertyException(
            UnrecognizedPropertyException e,
            HttpServletRequest request
    ) {
        String message = String.format("Unrecognized property in request for '%s': %s",
                request.getRequestURI(),
                e.getMessage());

        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                message,
                URI.create(request.getRequestURI()),
                e,
                "UNRECOGNIZED_PROPERTY"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ApiResponse<Map<String, Object>> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException e,
            HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Missing required request parameter",
                URI.create(request.getRequestURI()),
                e,
                "MISSING_PARAMETER"
        );

        return ApiResponse.error(CommonErrorCode.INVALID_REQUEST, HttpStatus.BAD_REQUEST);
    }

}
