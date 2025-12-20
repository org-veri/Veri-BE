package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.Authenticator;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;
import org.veri.be.global.auth.token.TokenProvider;
import org.veri.be.lib.exception.CommonErrorInfo;
import org.veri.be.lib.exception.http.BadRequestException;

import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements Authenticator {

    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;
    private final TokenBlacklistStore tokenBlacklistStore;

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;
    private final Clock clock;

    public LoginResponse login(Member member) {
        TokenProvider.TokenGeneration accessToken = tokenProvider.generateAccessToken(JwtClaimsPayload.from(member));
        TokenProvider.TokenGeneration refreshToken = tokenProvider.generateRefreshToken(member.getId());
        tokenStorageService.addRefreshToken(member.getId(), refreshToken.token(), refreshToken.expiredAt());
        return LoginResponse.builder()
                .accessToken(accessToken.token())
                .refreshToken(refreshToken.token())
                .build();
    }

    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshToken = request.getRefreshToken();

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException(CommonErrorInfo.INVALID_REQUEST);
        }
        
        if (tokenBlacklistStore.isBlackList(refreshToken)) {
            throw new org.veri.be.lib.exception.http.UnAuthorizedException(org.veri.be.global.auth.AuthErrorInfo.UNAUTHORIZED);
        }

        Object rawId = tokenProvider.parseRefreshToken(refreshToken).get("id");
        Long id = rawId == null ? null : ((Number) rawId).longValue();

        String storedToken = tokenStorageService.getRefreshToken(id);
        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new org.veri.be.lib.exception.http.UnAuthorizedException(org.veri.be.global.auth.AuthErrorInfo.UNAUTHORIZED);
        }

        Member member = memberQueryService.findById(id);
        String accessToken = tokenProvider.generateAccessToken(
                new JwtClaimsPayload(member.getId(), member.getEmail(), member.getNickname(), false)
        ).token();

        return ReissueTokenResponse.builder()
                .accessToken(accessToken)
                .build();
    }

    public void logout(String accessToken) {
        Object rawId = tokenProvider.parseAccessToken(accessToken).get("id");
        Long id = rawId == null ? null : ((Number) rawId).longValue();
        String refreshToken = tokenStorageService.getRefreshToken(id);
        tokenStorageService.deleteRefreshToken(id);

        // access token 만료시간 계산
        java.util.Date accessExpDate = tokenProvider.parseAccessToken(accessToken).getExpiration();
        java.time.Instant accessExp = accessExpDate != null ? accessExpDate.toInstant() : null;
        long now = Instant.now(clock).toEpochMilli();
        long accessRemainMs = (accessExp != null) ? accessExp.toEpochMilli() - now : 0L;
        if (accessRemainMs > 0) {
            tokenBlacklistStore.addBlackList(accessToken, accessRemainMs);
        }

        // refresh token이 존재하면 만료시간 계산 후 블랙리스트 등록
        if (refreshToken != null) {
            java.util.Date refreshExpDate = tokenProvider.parseRefreshToken(refreshToken).getExpiration();
            java.time.Instant refreshExp = refreshExpDate != null ? refreshExpDate.toInstant() : null;
            long refreshRemainMs = (refreshExp != null) ? refreshExp.toEpochMilli() - now : 0L;
            if (refreshRemainMs > 0) {
                tokenBlacklistStore.addBlackList(refreshToken, refreshRemainMs);
            }
        }
    }

    public LoginResponse loginWithOAuth2(OAuth2UserInfo userInfo) {
        Member member = saveOrGetMember(userInfo);
        return this.login(member);
    }

    private Member saveOrGetMember(OAuth2UserInfo request) {
        Optional<Member> optional = memberRepository.findByProviderIdAndProviderType(request.getProviderId(), request.getProviderType());
        if (optional.isPresent()) {
            return optional.get();
        } else {
            Member member = request.toMember();
            if (memberQueryService.existsByNickname(member.getNickname())) {
                member.updateInfo(
                        member.getNickname() + "_" + clock.millis(),
                        member.getProfileImageUrl());
            }
            return memberRepository.save(member);
        }
    }
}
