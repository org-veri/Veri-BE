package org.veri.be.mock;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.veri.be.member.entity.Member;
import org.veri.be.member.repository.MemberRepository;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.token.TokenProvider;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class MockTokenController {

    private final MemberRepository memberRepository;
    private final TokenProvider tokenProvider;

    @GetMapping("/api/v1/oauth2/mock/{memberId}")
    public LoginResponse getMockToken(@PathVariable Long memberId) {
        Member member;
        member = memberRepository.findById(memberId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.INVALID_REQUEST));


        return LoginResponse.builder()
                .accessToken(tokenProvider.generateAccessToken(
                        JwtClaimsPayload.of(member.getId(), member.getEmail(), member.getNickname(), false)
                ).token())
                .refreshToken(tokenProvider.generateRefreshToken(member.getId()).token())
                .build();
    }
}
