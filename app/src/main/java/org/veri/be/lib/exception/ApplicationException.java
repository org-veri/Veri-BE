package org.veri.be.lib.exception;

import lombok.Getter;
import org.jspecify.annotations.NullMarked;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponse;

import java.util.Objects;

@Getter
public abstract class ApplicationException extends RuntimeException implements ErrorResponse {

    protected final transient ErrorCode errorCode;
    private final String detailOverride;

    protected ApplicationException(ErrorCode errorCode, String detailOverride) {
        super(errorCode != null ? errorCode.getMessage() : null);
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        this.detailOverride = detailOverride;
    }

    public static ApplicationException of(ErrorCode errorCode) {
        return new ApplicationException(errorCode, null) {
        };
    }

    public static ApplicationException of(ErrorCode errorCode, String detailOverride) {
        return new ApplicationException(errorCode, detailOverride) {
        };
    }

    @Override
    @NullMarked
    public HttpStatusCode getStatusCode() {
        return errorCode.getStatus();
    }

    @Override
    @NullMarked
    public ProblemDetail getBody() {
        return ProblemDetail.forStatusAndDetail(errorCode.getStatus(),
                detailOverride == null ? errorCode.getMessage() : detailOverride);
    }
}
