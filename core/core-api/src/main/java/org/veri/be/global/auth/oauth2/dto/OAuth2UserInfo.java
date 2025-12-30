package org.veri.be.global.auth.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import org.veri.be.member.entity.Member;
import org.veri.be.member.entity.enums.ProviderType;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String nickname;
    private String image;
    private String providerId;
    private ProviderType providerType;

    public Member toMember() {
        return Member.builder()
                .email(this.getEmail())
                .nickname(this.getNickname())
                .profileImageUrl(this.getImage())
                .providerId(this.getProviderId())
                .providerType(this.getProviderType())
                .build();
    }
}
