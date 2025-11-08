package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.auth.converter.AuthConverter;
import org.veri.be.domain.auth.dto.AuthRequest;
import org.veri.be.domain.auth.dto.AuthResponse;
import org.veri.be.domain.auth.exception.AuthErrorInfo;
import org.veri.be.domain.auth.service.oauth2.OAuth2Service;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.exception.MemberErrorInfo;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.lib.auth.jwt.JwtUtil;
import org.veri.be.lib.exception.http.BadRequestException;
import org.veri.be.lib.exception.http.NotFoundException;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2Service kakaoOAuth2Service;

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
        Long id = (Long) JwtUtil.parseRefreshTokenPayloads(refreshToken).get("id");

        Member member = memberRepository.findById(id).orElseThrow(() ->
                new NotFoundException(MemberErrorInfo.NOT_FOUND));
        return AuthConverter.toReissueTokenResponse(
                JwtUtil.generateAccessToken(
                        new JwtClaimsPayload(member.getId(), member.getEmail(), member.getNickname(), false)
                )
        );
    }

    public void logout(String accessToken) {
        Long id = (Long) JwtUtil.parseAccessTokenPayloads(accessToken).get("id");
        String refreshToken = tokenStorageService.getRefreshToken(id);
        tokenStorageService.deleteRefreshToken(id);

        // access token 만료시간 계산
        java.util.Date accessExpDate = JwtUtil.parseAccessTokenPayloads(accessToken).getExpiration();
        java.time.Instant accessExp = accessExpDate != null ? accessExpDate.toInstant() : null;
        long now = java.time.Instant.now().toEpochMilli();
        long accessRemainMs = (accessExp != null) ? accessExp.toEpochMilli() - now : 0L;
        if (accessRemainMs > 0) {
            tokenStorageService.addBlackList(accessToken, accessRemainMs);
        }

        // refresh token이 존재하면 만료시간 계산 후 블랙리스트 등록
        if (refreshToken != null) {
            java.util.Date refreshExpDate = JwtUtil.parseRefreshTokenPayloads(refreshToken).getExpiration();
            java.time.Instant refreshExp = refreshExpDate != null ? refreshExpDate.toInstant() : null;
            long refreshRemainMs = (refreshExp != null) ? refreshExp.toEpochMilli() - now : 0L;
            if (refreshRemainMs > 0) {
                tokenStorageService.addBlackList(refreshToken, refreshRemainMs);
            }
        }
    }
}
