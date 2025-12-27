package org.veri.be.lib.exception.handler;

import com.fasterxml.jackson.databind.exc.UnrecognizedPropertyException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValidationErrorMapper {

    private static final String KEY_FIELD = "field";
    private static final String KEY_MESSAGE = "message";
    private static final String DEFAULT_MESSAGE = "Invalid";

    public List<Map<String, String>> from(Exception e) {
        if (e instanceof MethodArgumentNotValidException ex) {
            return extractFrom(ex);
        } else if (e instanceof ConstraintViolationException ex) {
            return extractFrom(ex);
        } else if (e instanceof HandlerMethodValidationException ex) {
            return extractFrom(ex);
        } else if (e instanceof UnrecognizedPropertyException ex) {
            return extractFrom(ex);
        } else if (e instanceof MissingServletRequestParameterException ex) {
            return extractFrom(ex);
        }
        return List.of();
    }

    private List<Map<String, String>> extractFrom(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
    }

    private List<Map<String, String>> extractFrom(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        KEY_FIELD, v.getPropertyPath() == null ? "" : v.getPropertyPath().toString(),
                        KEY_MESSAGE, Objects.requireNonNullElse(v.getMessage(), DEFAULT_MESSAGE)
                ))
                .toList();
    }

    private List<Map<String, String>> extractFrom(HandlerMethodValidationException ex) {
        try {
            return ex.getParameterValidationResults().stream()
                    .flatMap(r -> r.getResolvableErrors().stream().map(err -> {
                        String field = r.getMethodParameter().getParameterName() == null
                                ? ""
                                : r.getMethodParameter().getParameterName();
                        String message = err.getDefaultMessage() != null ? err.getDefaultMessage() : DEFAULT_MESSAGE;
                        return Map.of(KEY_FIELD, field, KEY_MESSAGE, message);
                    }))
                    .toList();
        } catch (Exception _) {
            return List.of();
        }
    }

    private List<Map<String, String>> extractFrom(UnrecognizedPropertyException ex) {
        return List.of(Map.of(
                KEY_FIELD, ex.getPropertyName(),
                KEY_MESSAGE, "Unrecognized field"
        ));
    }

    private List<Map<String, String>> extractFrom(MissingServletRequestParameterException ex) {
        return List.of(Map.of(
                KEY_FIELD, ex.getParameterName(),
                KEY_MESSAGE, "Missing required parameter"
        ));
    }

    private Map<String, String> toFieldError(FieldError fieldError) {
        return Map.of(
                KEY_FIELD, fieldError.getField(),
                KEY_MESSAGE, Objects.requireNonNullElse(fieldError.getDefaultMessage(), DEFAULT_MESSAGE)
        );
    }
}
