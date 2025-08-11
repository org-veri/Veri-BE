package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.auth.exception.AuthErrorInfo;
import org.goorm.veri.veribe.domain.auth.service.oauth2.OAuth2Service;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.exception.MemberErrorInfo;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.goorm.veri.veribe.global.jwt.JwtAuthenticator;
import org.goorm.veri.veribe.global.jwt.JwtExtractor;
import org.goorm.veri.veribe.global.jwt.JwtProvider;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2Service kakaoOAuth2Service;
    private final JwtProvider jwtProvider;
    private final JwtAuthenticator jwtAuthenticator;
    private final JwtExtractor jwtExtractor;

    private final MemberRepository memberRepository;
    private final TokenStorageService tokenStorageService;

    public AuthResponse.LoginResponse login(String provider, String code, String origin) {
        if (provider.equalsIgnoreCase(ProviderType.KAKAO.name())) {
            return kakaoOAuth2Service.login(code, origin);
        } else {
            throw new BadRequestException(AuthErrorInfo.UNSUPPORTED_OAUTH2_PROVIDER);
        }
    }

    public AuthResponse.ReissueTokenResponse reissueToken(AuthRequest.AuthReissueRequest request) {
        String refreshToken = request.getRefreshToken();
        jwtAuthenticator.verifyRefreshToken(refreshToken);
        String subject = jwtExtractor.parseRefreshTokenPayloads(refreshToken).getSubject();
        Long id = null;
        try {
            id = Long.valueOf(subject);
        } catch (Exception e) {
            throw new NotFoundException(MemberErrorInfo.NOT_FOUND);
        }
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new NotFoundException(MemberErrorInfo.NOT_FOUND));
        return AuthConverter.toReissueTokenResponse(jwtProvider.generateAccessToken(member.getId(), member.getNickname(), false));
    }

    public void logout(String accessToken) {
        String subject = jwtExtractor.parseAccessTokenPayloads(accessToken).getSubject();
        Long userId = null;
        try {
            userId = Long.valueOf(subject);
        } catch (Exception e) {
            return;
        }
        String refreshToken = tokenStorageService.getRefreshToken(userId);
        tokenStorageService.deleteRefreshToken(userId);

        // access token 만료시간 계산
        java.util.Date accessExpDate = jwtExtractor.parseAccessTokenPayloads(accessToken).getExpiration();
        java.time.Instant accessExp = accessExpDate != null ? accessExpDate.toInstant() : null;
        long now = java.time.Instant.now().toEpochMilli();
        long accessRemainMs = (accessExp != null) ? accessExp.toEpochMilli() - now : 0L;
        if (accessRemainMs > 0) {
            tokenStorageService.addBlackList(accessToken, accessRemainMs);
        }

        // refresh token이 존재하면 만료시간 계산 후 블랙리스트 등록
        if (refreshToken != null) {
            java.util.Date refreshExpDate = jwtExtractor.parseRefreshTokenPayloads(refreshToken).getExpiration();
            java.time.Instant refreshExp = refreshExpDate != null ? refreshExpDate.toInstant() : null;
            long refreshRemainMs = (refreshExp != null) ? refreshExp.toEpochMilli() - now : 0L;
            if (refreshRemainMs > 0) {
                tokenStorageService.addBlackList(refreshToken, refreshRemainMs);
            }
        }
    }
}
