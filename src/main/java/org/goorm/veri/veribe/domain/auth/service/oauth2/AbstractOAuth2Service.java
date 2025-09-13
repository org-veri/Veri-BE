package org.goorm.veri.veribe.domain.auth.service.oauth2;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.AuthConverter;
import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;
import org.goorm.veri.veribe.domain.auth.service.TokenCommandService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.goorm.veri.veribe.domain.member.service.MemberQueryService;

import java.util.Optional;

@RequiredArgsConstructor
public abstract class AbstractOAuth2Service implements OAuth2Service {

    protected final ProviderType providerType;
    protected final MemberRepository memberRepository;
    protected final MemberQueryService memberQueryService;
    protected final TokenCommandService tokenCommandService;

    @Override
    public AuthResponse.LoginResponse login(String code, String origin) {
        String accessToken = getAccessToken(code, getRedirectUri(origin));
        AuthRequest.OAuth2LoginUserInfo request = getUserInfo(accessToken);
        Member member = saveOrGetMember(request);
        return tokenCommandService.createLoginToken(member);
    }

    protected abstract String getAccessToken(String code, String redirectUri);

    protected abstract AuthRequest.OAuth2LoginUserInfo getUserInfo(String token);

    private Member saveOrGetMember(AuthRequest.OAuth2LoginUserInfo request) {
        Optional<Member> optional = memberRepository.findByProviderIdAndProviderType(request.getProviderId(), request.getProviderType());
        if (optional.isPresent()) {
            return optional.get();
        } else {
            Member member = AuthConverter.toMember(request);
            if (memberQueryService.existsByNickname(member.getNickname())) {
                member.updateInfo(
                        member.getNickname() + "_" + System.currentTimeMillis(),
                        member.getProfileImageUrl());
            }
            return memberRepository.save(member);
        }
    }

    private String getRedirectUri(String origin) {
        if (origin == null || origin.isEmpty()) {
            return getDefaultRedirectUri();
        }
        return origin + "/oauth/callback/" + providerType.name().toLowerCase();
    }

    protected abstract String getDefaultRedirectUri();
}
