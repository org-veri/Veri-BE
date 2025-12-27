package org.veri.be.global.auth.oauth2.dto;

import lombok.experimental.UtilityClass;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Map;
import java.util.function.Function;

@UtilityClass
public class OAuth2UserInfoMapper {

    public static OAuth2UserInfo of(CustomOAuth2User oAuth2User) {
        Function<Map<String, Object>, OAuth2UserInfo> mapper = providerMappers.get(oAuth2User.getProviderType());
        if (mapper == null) {
            throw ApplicationException.of(AuthErrorInfo.UNSUPPORTED_OAUTH2_PROVIDER);
        }
        return mapper.apply(oAuth2User.getAttributes());
    }

    private static final Map<ProviderType, Function<Map<String, Object>, OAuth2UserInfo>> providerMappers = Map.of(
            ProviderType.KAKAO, OAuth2UserInfoMapper::ofKakao
    );


    private static OAuth2UserInfo ofKakao(java.util.Map<String, Object> attributes) {
        Map<String, Object> account = (Map<String, Object>) attributes.get("kakao_account");
        Map<String, Object> profile = (Map<String, Object>) account.get("profile");

        return OAuth2UserInfo.builder()
                .email((String) account.get("email"))
                .nickname((String) profile.get("nickname"))
                .image((String) profile.get("profile_image_url"))
                .providerId(attributes.get("id").toString())
                .providerType(ProviderType.KAKAO)
                .build();
    }
}

