package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.security.core.GrantedAuthority
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.oauth2.dto.CustomOAuth2User
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfoMapper

class OAuth2UserInfoMapperTest {

    @Nested
    @DisplayName("of")
    inner class Of {

        @Test
        @DisplayName("Kakao 사용자 정보를 매핑한다")
        fun mapsKakaoUser() {
            val attributes = mapOf(
                "id" to 12345L,
                "kakao_account" to mapOf(
                    "email" to "member@test.com",
                    "profile" to mapOf(
                        "nickname" to "member",
                        "profile_image_url" to "https://example.com/profile.png"
                    )
                )
            )
            val user = CustomOAuth2User(listOf(), attributes, "id", "kakao")

            val info: OAuth2UserInfo = OAuth2UserInfoMapper.of(user)

            assertThat(info.email).isEqualTo("member@test.com")
            assertThat(info.nickname).isEqualTo("member")
            assertThat(info.image).isEqualTo("https://example.com/profile.png")
            assertThat(info.providerId).isEqualTo("12345")
            assertThat(info.providerType).isEqualTo(ProviderType.KAKAO)
        }

        @Test
        @DisplayName("지원하지 않는 ProviderType이면 예외가 발생한다")
        fun throwsWhenUnsupported() {
            val attributes = mapOf<String, Any>()
            val authorities = listOf<GrantedAuthority>()

            assertThrows(IllegalArgumentException::class.java) {
                CustomOAuth2User(authorities, attributes, "id", "google")
            }
        }
    }
}
