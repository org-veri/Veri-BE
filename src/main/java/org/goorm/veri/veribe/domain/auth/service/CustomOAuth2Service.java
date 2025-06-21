package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.converter.OAuth2Converter;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2ErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2Exception;
import org.goorm.veri.veribe.domain.auth.extractor.OAuth2UserExtractor;
import org.goorm.veri.veribe.domain.auth.extractor.OAuth2UserExtractorFactory;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class CustomOAuth2Service extends DefaultOAuth2UserService {

    private final MemberRepository memberRepository;
    private final OAuth2UserExtractorFactory oAuth2UserExtractorFactory;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(userRequest);

        OAuth2Request.OAuth2LoginRequest loginRequest = getLoginRequest(userRequest, oAuth2User);
        saveOrUpdateMember(loginRequest);
        return oAuth2User;
    }

    private OAuth2Request.OAuth2LoginRequest getLoginRequest(OAuth2UserRequest request, OAuth2User oAuth2User) {
        OAuth2UserExtractor extractor;
        try {
            extractor = oAuth2UserExtractorFactory.getExtractor(ProviderType.valueOf(request.getClientRegistration().getClientName().toUpperCase()));
            if (extractor == null) {
                throw new OAuth2Exception(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
            }
        } catch (Exception e) {
            throw new OAuth2Exception(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        }
        return extractor.extractUser(oAuth2User);
    }

    private void saveOrUpdateMember(OAuth2Request.OAuth2LoginRequest request) {
        Member member;
        Optional<Member> optional = memberRepository.findByProviderIdAndProviderType(request.getProviderId(), request.getProviderType());
        if (optional.isPresent()) {
            member = optional.get();
            member.updateInfo(request.getNickname(), request.getImage());
        }
        else {
            member = OAuth2Converter.toMember(request);
        }
        memberRepository.save(member);
    }
}
