package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.auth.AuthErrorInfo
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.auth.context.CurrentMemberAccessor
import org.veri.be.support.assertion.ExceptionAssertions
import java.util.Optional

class CurrentMemberAccessorTest {

    @Nested
    @DisplayName("getMemberOrThrow")
    inner class GetMemberOrThrow {

        @Test
        @DisplayName("현재 회원이 있으면 반환한다")
        fun returnsMember() {
            val member = member()
            val accessor = CurrentMemberAccessor { Optional.of(member) }

            val result = accessor.getMemberOrThrow()

            assertThat(result).isEqualTo(member)
        }

        @Test
        @DisplayName("현재 회원이 없으면 예외가 발생한다")
        fun throwsWhenMissing() {
            val accessor = CurrentMemberAccessor { Optional.empty() }

            ExceptionAssertions.assertApplicationException(
                accessor::getMemberOrThrow,
                AuthErrorInfo.UNAUTHORIZED
            )
        }
    }

    private fun member(): Member {
        return Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
