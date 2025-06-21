package org.goorm.veri.veribe.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;

public class OAuth2Request {

    @Getter
    @Builder
    public static class OAuth2LoginRequest {
        private String email;
        private String nickname;
        private String image;
        private String providerId;
        private ProviderType providerType;


    }
}
