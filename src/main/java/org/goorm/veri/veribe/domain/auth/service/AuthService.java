package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2ErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2Exception;
import org.goorm.veri.veribe.domain.auth.service.oauth2.OAuth2Service;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.exception.MemberErrorCode;
import org.goorm.veri.veribe.domain.member.exception.MemberException;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.global.util.JwtUtil;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final OAuth2Service kakaoOAuth2Service;
    private final JwtUtil jwtUtil;

    private final MemberRepository memberRepository;
    private final TokenStorageService tokenStorageService;

    public AuthResponse.LoginResponse login(String provider, String code) {
        if (provider.equalsIgnoreCase(ProviderType.KAKAO.name())) {
            return kakaoOAuth2Service.login(code);
        } else {
            throw new OAuth2Exception(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        }
    }

    public AuthResponse.ReissueTokenResponse reissueToken(AuthRequest.AuthReissueRequest request) {
        Long id = jwtUtil.getUserId(request.getRefreshToken());
        Member member = memberRepository.findById(id).orElseThrow(() ->
                new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
        return AuthConverter.toReissueTokenResponse(jwtUtil.createAccessToken(member));
    }

    public void logout(String accessToken) {
        Long userId = jwtUtil.getUserId(accessToken);
        String refreshToken = tokenStorageService.getRefreshToken(userId);
        tokenStorageService.deleteRefreshToken(userId);

        // access token 만료시간 계산
        java.time.Instant accessExp = jwtUtil.getExpirationInstant(accessToken);
        long now = java.time.Instant.now().toEpochMilli();
        long accessRemainMs = (accessExp != null) ? accessExp.toEpochMilli() - now : 0L;
        if (accessRemainMs > 0) {
            tokenStorageService.addBlackList(accessToken, accessRemainMs);
        }

        // refresh token이 존재하면 만료시간 계산 후 블랙리스트 등록
        if (refreshToken != null) {
            java.time.Instant refreshExp = jwtUtil.getExpirationInstant(refreshToken);
            long refreshRemainMs = (refreshExp != null) ? refreshExp.toEpochMilli() - now : 0L;
            if (refreshRemainMs > 0) {
                tokenStorageService.addBlackList(refreshToken, refreshRemainMs);
            }
        }
    }
}
