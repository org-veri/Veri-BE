package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCommandServiceImpl implements TokenCommandService {

    private final JwtUtil jwtUtil;
    private final TokenStorageService tokenStorageService;

    @Override
    public AuthResponse.LoginResponse createLoginToken(Member member) {
        String accessToken = jwtUtil.createAccessToken(member);
        String refreshToken = jwtUtil.createRefreshToken(member);
        tokenStorageService.addRefreshToken(member.getId(), refreshToken);
        return AuthConverter.toOAuth2LoginResponse(accessToken, refreshToken);
    }
}
