package org.goorm.veri.veribe.domain.auth.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.exception.TokenErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.TokenException;
import org.goorm.veri.veribe.domain.auth.service.TokenStorageService;
import org.goorm.veri.veribe.global.util.JwtUtil;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.http.HttpMethod;
import org.springframework.security.web.servlet.util.matcher.PathPatternRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtLogoutFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";
    private static final RequestMatcher REQUEST_MATCHER = PathPatternRequestMatcher.withDefaults().matcher(HttpMethod.POST, "/api/v1/auth/logout");

    private final TokenStorageService tokenStorageService;
    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        if (!requiresAuthentication(request))  {
            filterChain.doFilter(request, response);
        }
        else {
            String access = getToken(request);
            if (access == null) {
                throw new TokenException(TokenErrorCode.LOGOUT_TOKEN_NOT_EXIST);
            }

            this.processLogout(access);
            this.writeResponse(response);
        }
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private void processLogout(String accessToken) {
        Long userId = jwtUtil.getUserId(accessToken);
        String refreshToken = tokenStorageService.getRefreshToken(userId);
        tokenStorageService.deleteRefreshToken(userId);
        tokenStorageService.addBlackList(accessToken);
        tokenStorageService.addBlackList(refreshToken);
    }

    private boolean requiresAuthentication(HttpServletRequest request) {
        return REQUEST_MATCHER.matches(request);
    }

    private void writeResponse(HttpServletResponse response) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.writeValue(response.getOutputStream(), DefaultResponse.noContent());
    }

}
