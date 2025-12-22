package org.veri.be.lib.exception.handler;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ProblemDetail;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.veri.be.lib.exception.ErrorCode;

import java.net.URI;
import java.util.Map;

public class ProblemDetailFactory {

    private static final URI BLANK_URI = URI.create("about:blank");

    public ProblemDetail from(ErrorCode errorCode, String detail, WebRequest request) {
        ProblemDetail pd = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detail);

        pd.setType(BLANK_URI);
        pd.setTitle(resolveTitle(errorCode));
        pd.setInstance(resolveInstance(request));

        // 표준 확장 필드
        pd.setProperty("code", errorCode.getCode());

        // 레거시 호환 필드 (ApiResponse)
        pd.setProperty("isSuccess", false);
        pd.setProperty("message", detail);
        pd.setProperty("result", Map.of());

        return pd;
    }

    private String resolveTitle(ErrorCode errorCode) {
        return (errorCode instanceof Enum<?> e) ? e.name() : errorCode.getClass().getSimpleName();
    }

    private URI resolveInstance(WebRequest request) {
        if (request instanceof ServletWebRequest swr) {
            HttpServletRequest r = swr.getRequest();
            String uri = r.getRequestURI();
            String q = r.getQueryString();
            return URI.create(q == null ? uri : uri + "?" + q);
        }

        String description = request.getDescription(false); // e.g. uri=/api/sample
        if (description.startsWith("uri=")) {
            String uri = description.substring(4);
            if (!uri.isBlank()) {
                return URI.create(uri);
            }
        }
        return BLANK_URI;
    }
}
