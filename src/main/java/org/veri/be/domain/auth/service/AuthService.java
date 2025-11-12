package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.global.auth.Authenticator;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;
import org.veri.be.lib.auth.jwt.JwtUtil;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService implements Authenticator {

    private final MemberQueryService memberQueryService;
    private final TokenStorageService tokenStorageService;

    private final MemberRepository memberRepository;

    public LoginResponse login(Member member) {
        JwtUtil.TokenGeneration accessToken = JwtUtil.generateAccessToken(JwtClaimsPayload.from(member));
        JwtUtil.TokenGeneration refreshToken = JwtUtil.generateRefreshToken(member.getId());
        tokenStorageService.addRefreshToken(member.getId(), refreshToken.token(), refreshToken.expiredAt());
        return LoginResponse.builder()
                .accessToken(accessToken.token())
                .refreshToken(refreshToken.token())
                .build();
    }

    public ReissueTokenResponse reissueToken(ReissueTokenRequest request) {
        String refreshToken = request.getRefreshToken();
        Long id = (Long) JwtUtil.parseRefreshTokenPayloads(refreshToken).get("id");

        Member member = memberQueryService.findById(id);
        String accessToken = JwtUtil.generateAccessToken(
                new JwtClaimsPayload(member.getId(), member.getEmail(), member.getNickname(), false)
        ).token();

        return ReissueTokenResponse.builder()
                .accessToken(accessToken)
                .build();
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
                        member.getNickname() + "_" + System.currentTimeMillis(),
                        member.getProfileImageUrl());
            }
            return memberRepository.save(member);
        }
    }
}
