package org.veri.be.lib.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ErrorCode;

import java.util.List;
import java.util.Map;

@Slf4j
@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private final ProblemDetailFactory problemDetailFactory = new ProblemDetailFactory();
    private final ValidationErrorMapper validationErrorMapper = new ValidationErrorMapper();
    private final ExceptionLogger exceptionLogger = new ExceptionLogger();

    // 1) ApplicationException
    @ExceptionHandler(ApplicationException.class)
    public ResponseEntity<Object> handleApplicationException(ApplicationException e, WebRequest request) {
        ErrorCode errorCode = e.getErrorCode();
        String detail = e.getDetailOverride() == null ? errorCode.getMessage() : e.getDetailOverride();
        return build(errorCode, detail, request, e, "APPLICATION");
    }

    // 2) Validation (@RequestBody DTO)
    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers,
                                                                  HttpStatusCode status,
                                                                  WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.NOT_VALID_REQUEST_FIELDS; // V8888, 400
        ProblemDetail body = problemDetailFactory.from(errorCode, errorCode.getMessage(), request);

        List<Map<String, String>> errors = validationErrorMapper.from(ex);
        if (!errors.isEmpty()) {
            body.setProperty("errors", errors);
            body.setProperty("result", Map.of("errors", errors));
        }

        exceptionLogger.log(errorCode.getStatus(), body.getDetail(), body.getInstance(), ex, "VALIDATION");
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    // 3) Method validation
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<Object> handleHandlerMethodValidationException(HandlerMethodValidationException ex,
                                                                         WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
        return buildWithErrors(errorCode, errorCode.getMessage(), request, ex,
                validationErrorMapper.from(ex), "METHOD_VALIDATION");
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Object> handleConstraintViolationException(ConstraintViolationException ex,
                                                                     WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
        return buildWithErrors(errorCode, errorCode.getMessage(), request, ex,
                validationErrorMapper.from(ex), "CONSTRAINT_VIOLATION");
    }

    // 4) Binding / parse / param
    @ExceptionHandler({
            MethodArgumentTypeMismatchException.class,
            MissingServletRequestParameterException.class,
            HttpMessageNotReadableException.class,
            UnrecognizedPropertyException.class
    })
    public ResponseEntity<Object> handleBadRequest(Exception ex, WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.INVALID_REQUEST;
        return build(errorCode, errorCode.getMessage(), request, ex, "BAD_REQUEST");
    }

    // 5) Not found
    @ExceptionHandler({NoResourceFoundException.class, NoHandlerFoundException.class})
    public ResponseEntity<Object> handleNotFound(Exception ex, WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;
        return build(errorCode, errorCode.getMessage(), request, ex, "NOT_FOUND");
    }

    // 요청대로: Method Not Supported도 404로
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<Object> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                           WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.RESOURCE_NOT_FOUND;
        return build(errorCode, errorCode.getMessage(), request, ex, "METHOD_NOT_SUPPORTED");
    }

    // 6) Fallback
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Object> handleUnexpected(Exception ex, WebRequest request) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR; // E9999, 500
        return build(errorCode, errorCode.getMessage(), request, ex, "UNEXPECTED");
    }

    // --- Filter entrypoints (before controller) ---
    public ResponseEntity<ProblemDetail> handleApplicationExceptionBeforeController(
            ApplicationException e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = e.getErrorCode();
        String detail = e.getDetailOverride() == null ? errorCode.getMessage() : e.getDetailOverride();
        return buildProblem(errorCode, detail, new ServletWebRequest(request), e, "APPLICATION_FILTER");
    }

    public ResponseEntity<ProblemDetail> handleUnexpectedBeforeController(
            Exception e,
            HttpServletRequest request
    ) {
        ErrorCode errorCode = CommonErrorCode.INTERNAL_SERVER_ERROR;
        return buildProblem(errorCode, errorCode.getMessage(), new ServletWebRequest(request), e, "UNEXPECTED_FILTER");
    }

    // --- shared builders ---
    private ResponseEntity<Object> build(ErrorCode errorCode,
                                         String detail,
                                         WebRequest request,
                                         Exception ex,
                                         String tag) {
        ProblemDetail body = problemDetailFactory.from(errorCode, detail, request);
        exceptionLogger.log(errorCode.getStatus(), body.getDetail(), body.getInstance(), ex, tag);
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    private ResponseEntity<Object> buildWithErrors(ErrorCode errorCode,
                                                   String detail,
                                                   WebRequest request,
                                                   Exception ex,
                                                   List<Map<String, String>> errors,
                                                   String tag) {
        ProblemDetail body = problemDetailFactory.from(errorCode, detail, request);
        if (errors != null && !errors.isEmpty()) {
            body.setProperty("errors", errors);
            body.setProperty("result", Map.of("errors", errors));
        }
        exceptionLogger.log(errorCode.getStatus(), body.getDetail(), body.getInstance(), ex, tag);
        return ResponseEntity.status(errorCode.getStatus()).body(body);
    }

    private ResponseEntity<ProblemDetail> buildProblem(ErrorCode errorCode,
                                                       String detail,
                                                       WebRequest request,
                                                       Exception ex,
                                                       String tag) {
        ProblemDetail body = problemDetailFactory.from(errorCode, detail, request);
        exceptionLogger.log(errorCode.getStatus(), body.getDetail(), body.getInstance(), ex, tag);
        return ResponseEntity.status(errorCode.getStatus())
                .contentType(MediaType.APPLICATION_PROBLEM_JSON)
                .body(body);
    }
}
