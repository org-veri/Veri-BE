package org.veri.be.global.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class InjectIPAddressInterceptor implements HandlerInterceptor {

    public static final String IP = "IP";
    private static final String UNKNOWN = "unknown";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String clientIp = getClientIp(request);
        request.setAttribute(IP, clientIp);
        return true;
    }

    private String getClientIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");

        if (isValidIp(ip)) {
            if (ip.contains(",")) {
                return ip.split(",")[0].trim();
            }
            return ip;
        }

        ip = request.getHeader("X-Real-IP");
        if (isValidIp(ip)) return ip;

        ip = request.getHeader("Proxy-Client-IP");
        if (isValidIp(ip)) return ip;

        ip = request.getHeader("WL-Proxy-Client-IP");
        if (isValidIp(ip)) return ip;

        ip = request.getHeader("HTTP_CLIENT_IP");
        if (isValidIp(ip)) return ip;

        ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        if (isValidIp(ip)) return ip;

        ip = request.getRemoteAddr();

        if ("0:0:0:0:0:0:0:1".equals(ip)) {
            return "127.0.0.1";
        }

        return ip;
    }

    private boolean isValidIp(String ip) {
        return ip != null && !ip.isEmpty() && !UNKNOWN.equalsIgnoreCase(ip);
    }
}
