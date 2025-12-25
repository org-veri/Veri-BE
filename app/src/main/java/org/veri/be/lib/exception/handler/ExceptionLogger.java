package org.veri.be.lib.exception.handler;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class ExceptionLogger {

    public void log(HttpStatus status, String message, URI instance, Exception ex, String tag) {
        doLog(status, message, instance.toString(), null, ex, tag);
    }

    public void log(HttpStatus status, String message, HttpServletRequest request, Exception ex, String tag) {
        String path = request.getRequestURI();
        String requestDetails = LogUtils.getRequestDetails(request);

        doLog(status, message, path, requestDetails, ex, tag);
    }

    private void doLog(HttpStatus status, String message, String path, String requestDetails, Exception ex, String tag) {
        String detailsInfo = (requestDetails != null) ? " | " + requestDetails : "";

        if (status.is4xxClientError()) {
            log.info("[{}] {} (path: {}){}", tag, message, path, detailsInfo);

            if (log.isDebugEnabled()) {
                String optimizedStack = LogUtils.getOptimizedStackTrace(ex);
                log.debug("[{}][STACK]\n{}", tag, optimizedStack);
            }
            return;
        }

        String optimizedStack = LogUtils.getOptimizedStackTrace(ex);
        log.error("[{}] {} (path: {}){}\n{}", tag, message, path, detailsInfo, optimizedStack);
    }
}


@UtilityClass
class LogUtils {

    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ss")
            .setPrettyPrinting()
            .create();

    private static final String BASE_PACKAGE = "org.veri";

    private static final Set<String> SENSITIVE_HEADERS = new HashSet<>(Arrays.asList(
            "authorization", "cookie", "proxy-authorization", "x-auth-token"
    ));

    private static final String[] IGNORED_PREFIXES = {
            "org.springframework",
            "org.apache.catalina",
            "org.apache.coyote",
            "org.apache.tomcat",
            "jakarta.servlet",
            "javax.servlet",
            "java.lang.reflect",
            "jdk.internal.reflect",
            "sun.reflect",
            "java.base",
            "com.fasterxml.jackson",
            "io.undertow",
            "io.netty"
    };

    public static String getRequestDetails(HttpServletRequest request) {
        if (request == null) return " (Request is null)";

        Map<String, Object> details = new LinkedHashMap<>();

        details.put("method", request.getMethod());
        details.put("uri", request.getRequestURI());
        details.put("query", request.getQueryString());
        details.put("remoteAddr", request.getRemoteAddr());

        Map<String, String> headers = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames != null && headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            if (SENSITIVE_HEADERS.contains(headerName.toLowerCase())) {
                headers.put(headerName, "**** PROTECTED ****");
            } else {
                headers.put(headerName, request.getHeader(headerName));
            }
        }
        details.put("headers", headers);
        return gson.toJson(details);
    }

    public static String getOptimizedStackTrace(Throwable e) {
        if (e == null) return "";

        StringBuilder sb = new StringBuilder(e.toString());

        String filteredTrace = Arrays.stream(e.getStackTrace())
                .filter(LogUtils::isEssentialFrame)
                .map(StackTraceElement::toString)
                .collect(Collectors.joining("\n\tat "));

        if (!filteredTrace.isEmpty()) {
            sb.append("\n\tat ").append(filteredTrace);
        }

        if (e.getCause() != null) {
            sb.append("\nCaused by: ").append(getOptimizedStackTrace(e.getCause()));
        }

        return sb.toString();
    }

    private static boolean isEssentialFrame(StackTraceElement element) {
        String className = element.getClassName();

        if (className.startsWith(BASE_PACKAGE)) {
            return true;
        }

        for (String prefix : IGNORED_PREFIXES) {
            if (className.startsWith(prefix)) {
                return false;
            }
        }

        return true;
    }
}
