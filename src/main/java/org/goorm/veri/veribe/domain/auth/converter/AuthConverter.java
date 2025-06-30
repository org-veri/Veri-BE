package org.goorm.veri.veribe.domain.auth.converter;

import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;

public class AuthConverter {

    public static AuthRequest.OAuth2LoginUserInfo toOAuth2LoginRequest(String email, String nickname, String image, String providerId, ProviderType providerType) {
        return AuthRequest.OAuth2LoginUserInfo.builder()
                .email(email)
                .nickname(nickname)
                .image(image)
                .providerId(providerId)
                .providerType(providerType)
                .build();
    }

    public static Member toMember(AuthRequest.OAuth2LoginUserInfo request) {
        return Member.builder()
                .email(request.getEmail())
                .nickname(request.getNickname())
                .image(request.getImage())
                .providerId(request.getProviderId())
                .providerType(request.getProviderType())
                .build();
    }

    public static AuthResponse.LoginResponse toOAuth2LoginResponse(String access, String refresh) {
        return AuthResponse.LoginResponse.builder()
                .accessToken(access)
                .refreshToken(refresh)
                .build();
    }

    public static AuthResponse.ReissueTokenResponse toReissueTokenResponse(String access) {
        return AuthResponse.ReissueTokenResponse.builder()
                .accessToken(access)
                .build();
    }
}
