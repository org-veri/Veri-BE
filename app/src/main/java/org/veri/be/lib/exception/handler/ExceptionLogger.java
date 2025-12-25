package org.veri.be.lib.exception.handler;

import com.nimbusds.jose.shaded.gson.Gson;
import com.nimbusds.jose.shaded.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;

import java.net.URI;
import java.util.Arrays;
import java.util.stream.Collectors;

@Slf4j
public class ExceptionLogger {

    public void log(HttpStatus status, String message, URI instance, Exception ex, String tag) {
        if (status.is4xxClientError()) {
            log.info("[{}] {} (path: {})", tag, message, instance);

            if (log.isDebugEnabled()) {
                String optimizedStack = LogUtils.getOptimizedStackTrace(ex);
                log.debug("[{}][STACK] (path: {})\n{}", tag, instance, optimizedStack);
            }
            return;
        }

        String optimizedStack = LogUtils.getOptimizedStackTrace(ex);
        log.error("[{}] {} (path: {})\n{}", tag, message, instance, optimizedStack);
    }
}


class LogUtils {
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd'T'HH:mm:ss").create();
    private static final String BASE_PACKAGE = "org.veri";

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

    /**
     * 객체를 JSON 문자열로 변환 (기존 logBusiness의 가공 역할)
     */
    public static String toJson(Object data) {
        return gson.toJson(data);
    }

    /**
     * 예외의 스택 트레이스를 핵심만 남겨 문자열로 반환
     */
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
