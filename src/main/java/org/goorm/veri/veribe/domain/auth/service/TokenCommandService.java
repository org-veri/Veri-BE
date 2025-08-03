package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.data.JwtConfigData;
import org.goorm.veri.veribe.global.jwt.JwtProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCommandService {

    private final JwtProvider jwtProvider;
    private final TokenStorageService tokenStorageService;
    private final JwtConfigData jwtConfigData;

    public AuthResponse.LoginResponse createLoginToken(Member member) {
        String accessToken = jwtProvider.generateAccessToken(member.getId(), member.getNickname(), false);
        String refreshToken = jwtProvider.generateRefreshToken(member.getId());
        tokenStorageService.addRefreshToken(member.getId(), refreshToken, jwtConfigData.getRefresh().getValidity());
        return AuthConverter.toOAuth2LoginResponse(accessToken, refreshToken);
    }
}
