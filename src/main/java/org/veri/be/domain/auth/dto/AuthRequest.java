package org.veri.be.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.veri.be.domain.member.entity.enums.ProviderType;

public class AuthRequest {

    @Getter
    @Builder
    public static class OAuth2LoginUserInfo {
        private String email;
        private String nickname;
        private String image;
        private String providerId;
        private ProviderType providerType;
    }

    @Getter
    public static class AuthReissueRequest {
        private String refreshToken;
    }
}
