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
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.response.ApiResponse;

import java.util.List;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    private final ExceptionLogger exceptionLogger = new ExceptionLogger();
    private final ValidationErrorMapper validationErrorMapper = new ValidationErrorMapper();

    /**
     * 비즈니스 로직 예외
     */
    @ExceptionHandler(ApplicationException.class)
    public ApiResponse<Void> handleApplicationException(ApplicationException e, HttpServletRequest request) {
        exceptionLogger.log(
                e.getErrorCode().getStatus(),
                e.getMessage(),
                request,
                e,
                "APPLICATION_EXCEPTION"
        );
        return ApiResponse.error(e.getErrorCode(), e.getErrorCode().getStatus());
    }

    /**
     * 500 Unexpected Error
     */
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ApiResponse<Void> handleAnyUnexpectedException(Exception e, HttpServletRequest request) {
        exceptionLogger.log(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Unexpected Error Occurred",
                request,
                e,
                "UNEXPECTED_ERROR"
        );
        return ApiResponse.error(CommonErrorCode.INTERNAL_SERVER_ERROR, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /**
     * 404 Not Found - Router Resource Not Found
     * -> 봇 스캐닝이나 비정상적 접근일 확률 (Request 정보 로깅)
     */
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({
            NoHandlerFoundException.class,
            NoResourceFoundException.class,
            HttpRequestMethodNotSupportedException.class
    })
    public ApiResponse<Void> handleRouterNotFoundException(Exception e, HttpServletRequest request) {
        String message = String.format("'%s' '%s' not found", request.getMethod(), request.getRequestURI());

        exceptionLogger.log(
                HttpStatus.NOT_FOUND,
                message,
                request,
                e,
                "RESOURCE_NOT_FOUND"
        );
        return ApiResponse.error(CommonErrorCode.RESOURCE_NOT_FOUND, HttpStatus.NOT_FOUND);
    }


    /**
     * 400 Bad Request - Request Validation Failed
     */
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler({
            MethodArgumentNotValidException.class,
            HandlerMethodValidationException.class,
            ConstraintViolationException.class,
            UnrecognizedPropertyException.class,
            MissingServletRequestParameterException.class
    })
    public ApiResponse<List<Map<String, String>>> handleMethodArgumentNotValidException(Exception e, HttpServletRequest request) {

        exceptionLogger.log(HttpStatus.BAD_REQUEST, "Validation failed", request, e, e.getClass().getSimpleName().toUpperCase());
        List<Map<String, String>> errors = validationErrorMapper.from(e);
        return ApiResponse.error(CommonErrorCode.NOT_VALID_REQUEST_FIELDS, HttpStatus.BAD_REQUEST, errors);
    }
}
