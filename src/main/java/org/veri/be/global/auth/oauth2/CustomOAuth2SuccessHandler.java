package org.veri.be.global.auth.oauth2;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.common.contenttype.ContentType;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.Authenticator;
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfoMapper;
import org.veri.be.lib.response.ApiResponse;

import java.io.IOException;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final Authenticator authService;

    @Override
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {
        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();
        OAuth2UserInfo userInfo = OAuth2UserInfoMapper.of(oAuth2User);

        LoginResponse loginResponse = this.authService.loginWithOAuth2(userInfo);

        ObjectMapper objectMapper = new ObjectMapper();
        response.setContentType(ContentType.APPLICATION_JSON.getType());
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ApiResponse.ok(loginResponse)));
    }
}
