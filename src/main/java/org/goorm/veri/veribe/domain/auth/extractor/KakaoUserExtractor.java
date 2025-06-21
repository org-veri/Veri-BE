package org.goorm.veri.veribe.domain.auth.extractor;

import lombok.Getter;
import org.goorm.veri.veribe.domain.auth.converter.OAuth2Converter;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.member.entity.enums.ProviderType;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;

import java.util.LinkedHashMap;

@Getter
@Component
public class KakaoUserExtractor implements OAuth2UserExtractor {

    private final ProviderType providerType = ProviderType.KAKAO;

    @Override
    public OAuth2Request.OAuth2LoginRequest extractUser(OAuth2User oAuth2User) {
        LinkedHashMap<String, Object> properties = oAuth2User.getAttribute("properties");
        LinkedHashMap<String, Object> kakaoAccount = oAuth2User.getAttribute("kakao_account");

        String email = String.valueOf(kakaoAccount.get("email"));
        String nickname = String.valueOf(properties.get("nickname"));
        String profileImage = String.valueOf(properties.get("profile_image"));
        Long id = oAuth2User.getAttribute("id");

        return OAuth2Converter.toOAuth2LoginRequest(email, nickname, profileImage, String.valueOf(id), ProviderType.KAKAO);
    }

}
