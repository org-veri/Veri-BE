package org.veri.be.global.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.veri.be.domain.auth.service.oauth2.CustomAuthExceptionHandler;
import org.veri.be.domain.auth.service.oauth2.CustomOAuth2SuccessHandler;
import org.veri.be.domain.auth.service.oauth2.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomOAuth2UserService customOAuth2UserService;
    private final CustomOAuth2SuccessHandler customOAuth2SuccessHandler;
    private final CustomAuthExceptionHandler customAuthExceptionHandler;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests(authorize -> authorize
                .anyRequest().permitAll()
        );
        http.oauth2Login(config -> config
                .successHandler(customOAuth2SuccessHandler)
                .failureHandler(customAuthExceptionHandler)
                .userInfoEndpoint(endpointConfig -> endpointConfig
                        .userService(customOAuth2UserService)));

        return http.build();
    }
}
