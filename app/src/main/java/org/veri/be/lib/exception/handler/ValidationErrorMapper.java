package org.veri.be.lib.exception.handler;

import jakarta.validation.ConstraintViolationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ValidationErrorMapper {

    public List<Map<String, String>> from(MethodArgumentNotValidException ex) {
        return ex.getBindingResult().getFieldErrors().stream()
                .map(this::toFieldError)
                .toList();
    }

    public List<Map<String, String>> from(ConstraintViolationException ex) {
        return ex.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "field", v.getPropertyPath() == null ? "" : v.getPropertyPath().toString(),
                        "message", Objects.requireNonNullElse(v.getMessage(), "Invalid")
                ))
                .toList();
    }

    /**
     * 추출 실패 시 빈 리스트를 반환합니다.
     */
    public List<Map<String, String>> from(HandlerMethodValidationException ex) {
        try {
            return ex.getParameterValidationResults().stream()
                    .flatMap(r -> r.getResolvableErrors().stream().map(err -> {
                        String field = r.getMethodParameter().getParameterName() == null
                                ? ""
                                : r.getMethodParameter().getParameterName();
                        String message = err.getDefaultMessage() != null ? err.getDefaultMessage() : "Invalid";
                        return Map.of("field", field, "message", message);
                    }))
                    .toList();
        } catch (Exception _) {
            return List.of();
        }
    }

    private Map<String, String> toFieldError(FieldError fieldError) {
        return Map.of(
                "field", fieldError.getField(),
                "message", Objects.requireNonNullElse(fieldError.getDefaultMessage(), "Invalid")
        );
    }
}
