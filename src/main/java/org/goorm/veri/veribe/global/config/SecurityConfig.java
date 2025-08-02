package org.goorm.veri.veribe.global.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.filter.JwtExceptionFilter;
import org.goorm.veri.veribe.domain.auth.filter.JwtFilter;
import org.goorm.veri.veribe.domain.auth.service.TokenStorageService;
import org.goorm.veri.veribe.domain.member.service.MemberQueryService;
import org.goorm.veri.veribe.global.util.JwtUtil;
import org.namul.api.payload.code.dto.supports.DefaultResponseErrorReasonDTO;
import org.namul.api.payload.writer.FailureResponseWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final FailureResponseWriter<DefaultResponseErrorReasonDTO> failureResponseWriter;

    String[] allowUrl = {
            "/swagger-ui/**",
            "/swagger-resources/**",
            "/v3/api-docs/**",
            "/api/v1/oauth2/**",
            "/api/v1/auth/**",
            "/health-check"
    };

    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return web -> web.ignoring()
                .requestMatchers("/error", "/favicon.ico");
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(request -> request
                        .requestMatchers(allowUrl).permitAll()
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(jwtExceptionFilter(), JwtFilter.class)
                .securityContext(securityContext -> securityContext.securityContextRepository(
                        securityContextRepository()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .httpBasic(AbstractHttpConfigurer::disable)
                .csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .oauth2Login(Customizer.withDefaults())
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    Filter jwtFilter() {
        return new JwtFilter(jwtUtil, memberQueryService, tokenStorageService,
                securityContextRepository());
    }

    @Bean
    Filter jwtExceptionFilter() {
        return new JwtExceptionFilter(failureResponseWriter);
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }
}
