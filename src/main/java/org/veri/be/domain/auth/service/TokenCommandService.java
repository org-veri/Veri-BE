package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.auth.converter.AuthConverter;
import org.veri.be.domain.auth.dto.AuthResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.data.JwtConfigData;
import org.veri.be.global.jwt.JwtProvider;
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
