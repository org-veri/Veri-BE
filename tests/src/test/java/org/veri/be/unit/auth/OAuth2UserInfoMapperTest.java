package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.GrantedAuthority;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfoMapper;

class OAuth2UserInfoMapperTest {

    @Nested
    @DisplayName("of")
    class Of {

        @Test
        @DisplayName("Kakao 사용자 정보를 매핑한다")
        void mapsKakaoUser() {
            Map<String, Object> attributes = Map.of(
                    "id", 12345L,
                    "kakao_account", Map.of(
                            "email", "member@test.com",
                            "profile", Map.of(
                                    "nickname", "member",
                                    "profile_image_url", "https://example.com/profile.png"
                            )
                    )
            );
            CustomOAuth2User user = new CustomOAuth2User(List.of(), attributes, "id", "kakao");

            OAuth2UserInfo info = OAuth2UserInfoMapper.of(user);

            assertThat(info.getEmail()).isEqualTo("member@test.com");
            assertThat(info.getNickname()).isEqualTo("member");
            assertThat(info.getImage()).isEqualTo("https://example.com/profile.png");
            assertThat(info.getProviderId()).isEqualTo("12345");
            assertThat(info.getProviderType()).isEqualTo(ProviderType.KAKAO);
        }

        @Test
        @DisplayName("지원하지 않는 ProviderType이면 예외가 발생한다")
        void throwsWhenUnsupported() {
            Map<String, Object> attributes = Map.of();
            List<GrantedAuthority> authorities = List.of();

            org.junit.jupiter.api.Assertions.assertThrows(
                    IllegalArgumentException.class,
                    () -> new CustomOAuth2User(authorities, attributes, "id", "google")
            );
        }
    }
}
