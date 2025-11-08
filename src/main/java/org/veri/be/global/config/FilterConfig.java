package org.veri.be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.veri.be.domain.auth.service.TokenStorageService;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.lib.auth.jwt.JwtFilter;

@Configuration
@RequiredArgsConstructor
public class FilterConfig {

    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> firstFilterRegister() {
        return new FilterRegistrationBean<>(new JwtFilter(
                memberQueryService, tokenStorageService
        ));
    }
}
