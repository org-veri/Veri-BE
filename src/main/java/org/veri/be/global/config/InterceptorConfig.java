package org.veri.be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.veri.be.domain.auth.service.token.TokenStorageService;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.veri.be.lib.auth.jwt.JwtFilter;

import java.util.List;

@Configuration
@RequiredArgsConstructor
public class InterceptorConfig implements WebMvcConfigurer {

    private final AuthenticatedMemberResolver authenticatedMemberResolver;
    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(authenticatedMemberResolver);
    }

    @Bean
    public FilterRegistrationBean<OncePerRequestFilter> firstFilterRegister() {
        return new FilterRegistrationBean<>(new JwtFilter(
                memberQueryService, tokenStorageService
        ));
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InjectIPAddressInterceptor());
    }
}
