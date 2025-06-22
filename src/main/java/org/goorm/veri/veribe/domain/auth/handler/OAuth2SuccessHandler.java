package org.goorm.veri.veribe.domain.auth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.auth.service.TokenCommandService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.exception.MemberErrorCode;
import org.goorm.veri.veribe.domain.member.exception.MemberException;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final MemberRepository memberRepository;
    private final TokenCommandService tokenCommandService;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        Member member = this.getMember(authentication);
        OAuth2Response.OAuth2LoginResponse loginResponse = this.makeResponse(member);
        writeResponse(response, loginResponse);
    }

    private Member getMember(Authentication authentication) {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        Long id = oAuth2User.getAttribute("id");
        ProviderType providerType = ProviderType.valueOf((((OAuth2AuthenticationToken) authentication).getAuthorizedClientRegistrationId()).toUpperCase());
        return memberRepository.findByProviderIdAndProviderType(String.valueOf(id), providerType).orElseThrow(() ->
                new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    private OAuth2Response.OAuth2LoginResponse makeResponse(Member member) {
        return tokenCommandService.loginToken(member);
    }

    private void writeResponse(HttpServletResponse response, OAuth2Response.OAuth2LoginResponse loginResponse) throws IOException{
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(HttpStatus.OK.value());
        objectMapper.writeValue(response.getOutputStream(), DefaultResponse.ok(loginResponse));
    }

}
