package org.veri.be.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.veri.be.domain.auth.service.token.TokenStorageService;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.interceptors.InjectIPAddressInterceptor;
import org.veri.be.lib.auth.jwt.JwtFilter;

import java.util.Collections;
import java.util.List;

@Slf4j
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
    @ConditionalOnBooleanProperty(prefix = "auth.jwt", name = "use")
    public FilterRegistrationBean<OncePerRequestFilter> firstFilterRegister() {
        log.info("Filter jwt registered");
        FilterRegistrationBean<OncePerRequestFilter> registrationBean =
                new FilterRegistrationBean<>(new JwtFilter(memberQueryService, tokenStorageService));

        // 2. 실행 순서를 가장 높게(가장 먼저) 설정합니다.
        registrationBean.setOrder(Ordered.HIGHEST_PRECEDENCE);

        // 3. (선택 사항) 필터를 적용할 URL 패턴을 지정할 수 있습니다.
        //    설정하지 않으면 기본값은 '/*' (모든 요청)입니다.
        registrationBean.setUrlPatterns(Collections.singletonList("/api/*"));

        return registrationBean;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new InjectIPAddressInterceptor());
    }
}
