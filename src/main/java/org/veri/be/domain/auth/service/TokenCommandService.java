package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.auth.converter.AuthConverter;
import org.veri.be.domain.auth.dto.LoginResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.lib.auth.jwt.data.JwtProperties;
import org.veri.be.lib.auth.jwt.JwtUtil;

@Service
@RequiredArgsConstructor
public class TokenCommandService {

    private final TokenStorageService tokenStorageService;
    private final JwtProperties jwtProperties;

    public LoginResponse createLoginToken(Member member) {
        String accessToken = JwtUtil.generateAccessToken(JwtClaimsPayload.from(member));
        String refreshToken = JwtUtil.generateRefreshToken(member.getId());
        tokenStorageService.addRefreshToken(member.getId(), refreshToken, jwtProperties.getRefresh().getValidity());
        return AuthConverter.toOAuth2LoginResponse(accessToken, refreshToken);
    }
}
