package org.veri.be.lib.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.domain.auth.service.TokenBlacklistStore;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.token.TokenProvider;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final MemberQueryService memberQueryService;
    private final TokenBlacklistStore tokenBlacklistStore;
    private final TokenProvider tokenProvider;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);

            if (token != null && !tokenBlacklistStore.isBlackList(token)) {
                Object raw = tokenProvider.parseAccessToken(token).get("id");
                Long id = raw == null ? null : ((Number) raw).longValue();

                if (id != null) {
                    Member member = memberQueryService.findById(id);

                    MemberContext.setCurrentToken(token);
                    MemberContext.setCurrentMember(member);
                }
            }

            filterChain.doFilter(request, response);
        } finally {
            MemberContext.clear();
        }
    }
}
