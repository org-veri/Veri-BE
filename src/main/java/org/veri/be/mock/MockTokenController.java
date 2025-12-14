package org.veri.be.mock;

import lombok.RequiredArgsConstructor;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.lib.auth.jwt.JwtService;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
public class MockTokenController {

    private final MemberRepository memberRepository;
    private final JwtService jwtService;

    @GetMapping("/api/v1/oauth2/mock/{memberId}")
    public LoginResponse getMockToken(@PathVariable Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found with ID: " + memberId));

        return LoginResponse.builder()
                .accessToken(jwtService.generateAccessToken(JwtClaimsPayload.from(member)).token())
                .refreshToken(jwtService.generateRefreshToken(member.getId()).token())
                .build();
    }
}
