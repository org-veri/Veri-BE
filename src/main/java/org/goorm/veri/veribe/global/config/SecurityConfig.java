package org.goorm.veri.veribe.global.config;

import jakarta.servlet.Filter;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.filter.JwtExceptionFilter;
import org.goorm.veri.veribe.domain.auth.filter.JwtFilter;
import org.goorm.veri.veribe.domain.auth.service.TokenStorageService;
import org.goorm.veri.veribe.domain.member.service.MemberQueryService;
import org.goorm.veri.veribe.global.auth.CustomAuthorizationRequestResolver;
import org.goorm.veri.veribe.global.jwt.JwtAuthenticator;
import org.goorm.veri.veribe.global.jwt.JwtExtractor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizationRequestResolver;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.util.UriComponentsBuilder;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticator jwtAuthenticator;
    private final JwtExtractor jwtExtractor;
    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;
    private final AuthenticationEntryPoint authenticationEntryPoint;
    private final AccessDeniedHandler accessDeniedHandler;
    private final ClientRegistrationRepository clientRegistrationRepository;

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
                .oauth2Login(oauth2 -> oauth2
                        .authorizationEndpoint(authorization -> authorization
                                .authorizationRequestResolver(customAuthorizationRequestResolver(clientRegistrationRepository))
                        )
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    Filter jwtFilter() {
        return new JwtFilter(jwtAuthenticator, jwtExtractor, memberQueryService, tokenStorageService,
                securityContextRepository());
    }

    @Bean
    Filter jwtExceptionFilter() {
        return new JwtExceptionFilter();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new RequestAttributeSecurityContextRepository();
    }

    @Bean
    OAuth2AuthorizationRequestResolver customAuthorizationRequestResolver(ClientRegistrationRepository repo) {
        String authorizationRequestBaseUri = UriComponentsBuilder.fromPath("/oauth2/authorization")
                .build()
                .toUriString();
        return new CustomAuthorizationRequestResolver(repo, authorizationRequestBaseUri);
    }
}
