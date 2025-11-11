package org.veri.be.lib.auth.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.domain.auth.service.token.TokenStorageService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.context.MemberContext;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        String token = AuthorizationHeaderUtil.extractTokenFromAuthorizationHeader(request);
        if (token != null && !tokenStorageService.isBlackList(token)) {
            Long id = (Long) JwtUtil.parseRefreshTokenPayloads(token).get("id");
            if (id != null) {
                Member member = memberQueryService.findById(id);

                MemberContext.setToken(token);
                MemberContext.setMember(member);
            }
        }
        request.setAttribute("token", token);
        filterChain.doFilter(request, response);
    }
}
