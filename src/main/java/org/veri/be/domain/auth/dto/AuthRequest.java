package org.veri.be.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
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
