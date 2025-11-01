package org.veri.be.mock;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.auth.dto.AuthResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.jwt.JwtProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class MockTokenController {

    private final MemberRepository memberRepository;
    private final JwtProvider jwtProvider;

    @GetMapping("/api/v1/oauth2/mock/{memberId}")
    public AuthResponse.LoginResponse getMockToken(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        return AuthResponse.LoginResponse.builder()
                .accessToken(jwtProvider.generateAccessToken(member.getId(), member.getNickname(), false))
                .refreshToken(jwtProvider.generateRefreshToken(member.getId()))
                .build();
    }
}
