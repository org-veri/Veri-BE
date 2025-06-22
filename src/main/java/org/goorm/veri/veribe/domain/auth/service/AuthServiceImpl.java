package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2ErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.OAuth2Exception;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final OAuth2Service kakaoOAuth2Service;

    @Override
    public OAuth2Response.OAuth2LoginResponse login(String provider, String code) {
        if (provider.equalsIgnoreCase(ProviderType.KAKAO.name())) {
            return kakaoOAuth2Service.login(code);
        }
        else {
            throw new OAuth2Exception(OAuth2ErrorCode.UNSUPPORTED_OAUTH2_PROVIDER);
        }
    }
}
