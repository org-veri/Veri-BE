package org.veri.be.global.auth.oauth2.dto;

import lombok.Builder;
import lombok.Getter;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.ApplicationException;

import java.util.Map;
import java.util.function.Function;

@Getter
@Builder
public class OAuth2UserInfo {
    private String email;
    private String nickname;
    private String image;
    private String providerId;
    private ProviderType providerType;

    public static OAuth2UserInfo from(CustomOAuth2User oAuth2User) {
        Function<Map<String, Object>, OAuth2UserInfo> mapper = providerMappers.get(oAuth2User.getProviderType());
        if (mapper == null) {
            throw ApplicationException.of(AuthErrorInfo.UNSUPPORTED_OAUTH2_PROVIDER);
        }
        return mapper.apply(oAuth2User.getAttributes());
    }

    public Member toMember() {
        return Member.builder()
                .email(this.getEmail())
                .nickname(this.getNickname())
                .profileImageUrl(this.getImage())
                .providerId(this.getProviderId())
                .providerType(this.getProviderType())
                .build();
    }

    private static final Map<ProviderType, Function<Map<String, Object>, OAuth2UserInfo>> providerMappers = Map.of(
            ProviderType.KAKAO, OAuth2UserInfo::fromKakao
    );

    private static OAuth2UserInfo fromKakao(Map<String, Object> attributes) {
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
