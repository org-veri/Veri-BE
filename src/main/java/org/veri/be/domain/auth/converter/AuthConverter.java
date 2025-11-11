package org.veri.be.domain.auth.converter;

import org.veri.be.domain.auth.dto.LoginResponse;
import org.veri.be.domain.auth.dto.ReissueTokenResponse;
import org.veri.be.domain.auth.service.oauth2.dto.OAuth2UserInfo;
import org.veri.be.domain.member.entity.Member;

public class AuthConverter {

    public static Member toMember(OAuth2UserInfo request) {
        return Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .profileImageUrl(request.getImage())
                .providerId(request.getProviderId())
                .providerType(request.getProviderType())
                .build();
    }

    public static LoginResponse toOAuth2LoginResponse(String access, String refresh) {
        return LoginResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    public static ReissueTokenResponse toReissueTokenResponse(String access) {
        return ReissueTokenResponse.builder()
                .accessToken(access)
                .build();
    }
}
