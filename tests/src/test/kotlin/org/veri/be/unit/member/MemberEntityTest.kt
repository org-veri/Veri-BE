package org.veri.be.unit.member

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType

class MemberEntityTest {

    @Nested
    @DisplayName("updateInfo")
    inner class UpdateInfo {

        @Test
        @DisplayName("값이 전달되면 닉네임과 이미지를 변경한다")
        fun updatesFields() {
            val member = member("old", "https://example.com/old.png")

            member.updateInfo("new", "https://example.com/new.png")

            assertThat(member.nickname).isEqualTo("new")
            assertThat(member.profileImageUrl).isEqualTo("https://example.com/new.png")
        }

        @Test
        @DisplayName("null 값은 기존 값을 유지한다")
        fun keepsExistingValues() {
            val member = member("old", "https://example.com/old.png")

            member.updateInfo(null, null)

            assertThat(member.nickname).isEqualTo("old")
            assertThat(member.profileImageUrl).isEqualTo("https://example.com/old.png")
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    inner class AuthorizeMember {

        @Test
        @DisplayName("다른 사용자면 false를 반환다")
        fun falseWhenDifferentMember() {
            val member = member("member", "https://example.com/profile.png")

            assertThat(member.authorizeMember(2L)).isFalse()
        }

        @Test
        @DisplayName("본인 ID면 통과한다")
        fun allowsWhenSameMember() {
            val member = member("member", "https://example.com/profile.png")

            assertThatCode { member.authorizeMember(1L) }
                .doesNotThrowAnyException()
        }
    }

    private fun member(nickname: String, imageUrl: String): Member {
        return Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname(nickname)
            .profileImageUrl(imageUrl)
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
