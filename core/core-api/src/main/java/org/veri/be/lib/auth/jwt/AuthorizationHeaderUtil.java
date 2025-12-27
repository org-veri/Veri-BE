package org.veri.be.lib.auth.jwt;

import jakarta.servlet.http.HttpServletRequest;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class AuthorizationHeaderUtil {

    public static final String BEARER = "Bearer ";

    public static String extractTokenFromAuthorizationHeader(HttpServletRequest request) {
        String authorizationHeader = request.getHeader(AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith(BEARER)) {
            return null;
        }
        return authorizationHeader.substring(BEARER.length()).trim();
    }
}
