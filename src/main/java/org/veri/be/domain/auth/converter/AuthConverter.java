package org.veri.be.domain.auth.converter;

import org.veri.be.domain.auth.dto.AuthRequest;
import org.veri.be.domain.auth.dto.AuthResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;

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
                .profileImageUrl(request.getImage())
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
