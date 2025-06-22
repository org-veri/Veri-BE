package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.OAuth2Converter;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractOAuth2Service implements OAuth2Service {

    private final MemberRepository memberRepository;
    private final TokenCommandService tokenCommandService;

    @Override
    public OAuth2Response.OAuth2LoginResponse login(String code) {
        String accessToken = getAccessToken(code);
        OAuth2Request.OAuth2LoginRequest request = getUserInfo(accessToken);
        Member member = saveOrUpdateMember(request);
        return tokenCommandService.loginToken(member);
    }

    protected abstract String getAccessToken(String code);

    protected abstract OAuth2Request.OAuth2LoginRequest getUserInfo(String token);

    private Member saveOrUpdateMember(OAuth2Request.OAuth2LoginRequest request) {
        Member member;
        Optional<Member> optional = memberRepository.findByProviderIdAndProviderType(request.getProviderId(), request.getProviderType());
        if (optional.isPresent()) {
            member = optional.get();
            member.updateInfo(request.getNickname(), request.getImage());
        }
        else {
            member = OAuth2Converter.toMember(request);
        }
        return memberRepository.save(member);
    }
}
