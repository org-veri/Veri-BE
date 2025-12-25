package org.veri.be.lib.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.response.ApiResponse;

import java.net.URI;
import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ExceptionLogger exceptionLogger = new ExceptionLogger();
    private final ValidationErrorMapper validationErrorMapper = new ValidationErrorMapper();

    @ExceptionHandler(ApplicationException.class)
    public ApiResponse<Void> handleApplicationException(
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
    public ApiResponse<Void> handleAnyUnexpectedException(
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
    public ApiResponse<Void> handleNoResourceFoundException(
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
    public ApiResponse<Void> handleNoHandlerFoundException(
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
    public ApiResponse<Void> handleHttpRequestNotSupportedException(
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
    public ApiResponse<List<Map<String, String>>> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException e,
            HttpServletRequest request) {

        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Validation failed for request fields",
                URI.create(request.getRequestURI()),
                e,
                "VALIDATION_ERROR"
        );

        List<Map<String, String>> errors = validationErrorMapper.from(e);
        return ApiResponse.error(CommonErrorCode.NOT_VALID_REQUEST_FIELDS, HttpStatus.BAD_REQUEST, errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ApiResponse<List<Map<String, String>>> handleHandlerMethodValidationException(
            HandlerMethodValidationException e,
            HttpServletRequest request) {

        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Method parameter validation failed",
                URI.create(request.getRequestURI()),
                e,
                "METHOD_VALIDATION_ERROR"
        );

        List<Map<String, String>> errors = validationErrorMapper.from(e);
        return ApiResponse.error(CommonErrorCode.NOT_VALID_REQUEST_FIELDS, HttpStatus.BAD_REQUEST, errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ApiResponse<List<Map<String, String>>> handleConstraintViolationException(
            ConstraintViolationException e,
            HttpServletRequest request) {

        exceptionLogger.log(
                HttpStatus.BAD_REQUEST,
                "Constraint violation occurred",
                URI.create(request.getRequestURI()),
                e,
                "CONSTRAINT_VIOLATION"
        );

        List<Map<String, String>> errors = validationErrorMapper.from(e);
        return ApiResponse.error(CommonErrorCode.NOT_VALID_REQUEST_FIELDS, HttpStatus.BAD_REQUEST, errors);
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(UnrecognizedPropertyException.class)
    public ApiResponse<Void> handleUnrecognizedPropertyException(
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
    public ApiResponse<Void> handleMissingServletRequestParameterException(
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
