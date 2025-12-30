package org.veri.be.global.auth.oauth2.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String nickname;
    private String image;
    private String providerId;
    private String providerType;
}
