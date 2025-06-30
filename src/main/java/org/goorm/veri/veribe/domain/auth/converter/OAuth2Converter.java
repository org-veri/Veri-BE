package org.goorm.veri.veribe.domain.auth.converter;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;

public class OAuth2Converter {

    public static OAuth2Request.OAuth2LoginUserInfo toOAuth2LoginRequest(String email, String nickname, String image, String providerId, ProviderType providerType) {
        return OAuth2Request.OAuth2LoginUserInfo.builder()
                .email(email)
                .nickname(nickname)
                .image(image)
                .providerId(providerId)
                .providerType(providerType)
                .build();
    }

    public static Member toMember(OAuth2Request.OAuth2LoginUserInfo request) {
        return Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .image(request.getImage())
                .providerId(request.getProviderId())
                .providerType(request.getProviderType())
                .build();
    }

    public static OAuth2Response.LoginResponse toOAuth2LoginResponse(String access, String refresh) {
        return OAuth2Response.LoginResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    public static OAuth2Response.ReissueTokenResponse toReissueTokenResponse(String access) {
        return OAuth2Response.ReissueTokenResponse.builder()
                .accessToken(access)
                .build();
    }
}
