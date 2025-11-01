package org.veri.be.global.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.HandlerInterceptor;

public class InjectIPAddressInterceptor implements HandlerInterceptor {

    public static final String IP = "IP";

    @Override
    public boolean preHandle(
            HttpServletRequest request,
            HttpServletResponse response,
            Object handler
    ) {
        String clientIp = getClientIp(request);
        request.setAttribute(IP, clientIp);

        return true;
    }

    /**
     * 클라이언트 IP를 추출
     * <p>
     * X-Forwarded-For, Proxy-Client-IP, WL-Proxy-Client-IP 헤더를 우선적으로 확인하고, 그렇지 않으면
     * request.getRemoteAddr()를 사용합니다.
     */
    private String getClientIp(HttpServletRequest request) {
        String clientIp = request.getHeader("X-Forwarded-For");
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getHeader("WL-Proxy-Client-IP");
        }
        if (clientIp == null || clientIp.isEmpty() || "unknown".equalsIgnoreCase(clientIp)) {
            clientIp = request.getRemoteAddr();
        }
        return clientIp;
    }
}
