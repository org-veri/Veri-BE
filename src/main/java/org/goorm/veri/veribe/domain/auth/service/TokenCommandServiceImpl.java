package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.OAuth2Converter;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class TokenCommandServiceImpl implements TokenCommandService {

    private final JwtUtil jwtUtil;

    @Override
    public OAuth2Response.LoginResponse createLoginToken(Member member) {
        return OAuth2Converter.toOAuth2LoginResponse(jwtUtil.createAccessToken(member), jwtUtil.createRefreshToken(member));
    }
}
