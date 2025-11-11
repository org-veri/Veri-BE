package org.veri.be.domain.auth.service;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.veri.be.domain.auth.converter.OAuth2UserInfoMapper;
import org.veri.be.domain.auth.dto.LoginResponse;
import org.veri.be.domain.auth.service.oauth2.dto.OAuth2UserInfo;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final AuthService authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        OAuth2UserInfo userInfo = OAuth2UserInfoMapper.of(oAuth2User);

        LoginResponse loginResponse = this.authService.loginWithOAuth2(userInfo);

        response.getWriter().write(loginResponse.toString());
    }
}
