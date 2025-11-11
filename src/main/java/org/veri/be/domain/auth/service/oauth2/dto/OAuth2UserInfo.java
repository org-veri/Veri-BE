package org.veri.be.domain.auth.service.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import org.veri.be.domain.member.entity.enums.ProviderType;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String nickname;
    private String image;
    private String providerId;
    private ProviderType providerType;
}
