package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractOAuth2Service implements OAuth2Service {

    private final MemberRepository memberRepository;
    private final TokenCommandService tokenCommandService;

    @Override
    public AuthResponse.LoginResponse login(String code) {
        String accessToken = getAccessToken(code);
        AuthRequest.OAuth2LoginUserInfo request = getUserInfo(accessToken);
        Member member = saveOrUpdateMember(request);
        return tokenCommandService.createLoginToken(member);
    }

    protected abstract String getAccessToken(String code);

    protected abstract AuthRequest.OAuth2LoginUserInfo getUserInfo(String token);

    private Member saveOrUpdateMember(AuthRequest.OAuth2LoginUserInfo request) {
        Member member;
        Optional<Member> optional = memberRepository.findByProviderIdAndProviderType(request.getProviderId(), request.getProviderType());
        if (optional.isPresent()) {
            member = optional.get();
            member.updateInfo(request.getNickname(), request.getImage());
        }
        else {
            member = AuthConverter.toMember(request);
        }
        return memberRepository.save(member);
    }
}
