package org.veri.be.domain.auth.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.auth.service.TokenStorageService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.jwt.JwtAuthenticator;
import org.veri.be.global.jwt.JwtExtractor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;

@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String TOKEN_PREFIX = "Bearer ";

    private final JwtAuthenticator jwtAuthenticator;
    private final JwtExtractor jwtExtractor;
    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;
    private final SecurityContextRepository securityContextRepository;
    private final SecurityContextHolderStrategy securityContextHolderStrategy = SecurityContextHolder.getContextHolderStrategy();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String token = getToken(request);
        boolean valid = false;
        if (token != null && !tokenStorageService.isBlackList(token)) {
            try {
                jwtAuthenticator.verifyAccessToken(token);
                valid = true;
            } catch (Exception e) {
                valid = false;
            }
        }
        if (token != null && valid) {
            String subject = jwtExtractor.parseAccessTokenPayloads(token).getSubject();
            Long id = null;
            try {
                id = Long.valueOf(subject);
            } catch (Exception e) {
                id = null;
            }
            if (id != null) {
                Member member = memberQueryService.findById(id);
                Authentication authentication = createAuthentication(member);
                onAuthorization(request, response, authentication);
            }
        }
        request.setAttribute("token", token);
        filterChain.doFilter(request, response);
    }

    private String getToken(HttpServletRequest request) {
        String header = request.getHeader(AUTHORIZATION_HEADER);
        if (header != null && header.startsWith(TOKEN_PREFIX)) {
            return header.substring(TOKEN_PREFIX.length());
        }
        return null;
    }

    private Authentication createAuthentication(Member member) {
        return UsernamePasswordAuthenticationToken.authenticated(member, null, Collections.singleton(new SimpleGrantedAuthority("ROLE_USER")));
    }

    private void onAuthorization(HttpServletRequest request, HttpServletResponse response, Authentication authentication) {
        SecurityContext securityContext = securityContextHolderStrategy.createEmptyContext();
        securityContext.setAuthentication(authentication);
        securityContextHolderStrategy.setContext(securityContext);
        securityContextRepository.saveContext(securityContext, request, response);
    }
}
