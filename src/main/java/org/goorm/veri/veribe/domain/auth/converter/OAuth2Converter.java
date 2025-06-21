package org.goorm.veri.veribe.domain.auth.converter;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;

public class OAuth2Converter {

    public static OAuth2Request.OAuth2LoginRequest toOAuth2LoginRequest(String email, String nickname, String image, String providerId, ProviderType providerType) {
        return OAuth2Request.OAuth2LoginRequest.builder()
                .email(email)
                .nickname(nickname)
                .image(image)
                .providerId(providerId)
                .providerType(providerType)
                .build();
    }

    public static Member toMember(OAuth2Request.OAuth2LoginRequest request) {
        return Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .image(request.getImage())
                .providerId(request.getProviderId())
                .providerType(request.getProviderType())
                .build();
    }

    public static OAuth2Response.OAuth2LoginResponse toOAuth2LoginResponse(Long id, String access, String refresh) {
        return OAuth2Response.OAuth2LoginResponse.builder()
                .id(id)
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }
}
