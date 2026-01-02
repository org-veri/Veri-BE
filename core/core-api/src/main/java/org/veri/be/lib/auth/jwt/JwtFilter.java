package org.veri.be.lib.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.domain.auth.service.TokenBlacklistStore;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.token.TokenProvider;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final TokenBlacklistStore tokenBlacklistStore;
    private final TokenProvider tokenProvider;
    private final ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);

            if (token != null && !tokenBlacklistStore.isBlackList(token)) {
                Object raw = tokenProvider.parseAccessToken(token);
                JwtClaimsPayload payload = objectMapper.convertValue(raw, JwtClaimsPayload.class);
                Long id = payload.id();

                if (id != null) {
                    MemberContext.setCurrentToken(token);
                    MemberContext.setCurrentMemberInfo(payload);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MemberContext.clear();
        }
    }
}
