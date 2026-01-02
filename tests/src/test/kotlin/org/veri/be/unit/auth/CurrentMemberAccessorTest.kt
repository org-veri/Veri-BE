package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.auth.AuthErrorInfo
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
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
            val accessor = testAccessor(Optional.of(member), Optional.of(CurrentMemberInfo.from(member)))

            val result = accessor.getMemberOrThrow()

            assertThat(result).isEqualTo(member)
        }

        @Test
        @DisplayName("현재 회원이 없으면 예외가 발생한다")
        fun throwsWhenMissing() {
            val accessor = testAccessor(Optional.empty(), Optional.empty())

            ExceptionAssertions.assertApplicationException(
                accessor::getMemberOrThrow,
                AuthErrorInfo.UNAUTHORIZED
            )
        }
    }

    @Nested
    @DisplayName("getMemberInfoOrThrow")
    inner class GetMemberInfoOrThrow {

        @Test
        @DisplayName("현재 회원 정보가 있으면 반환한다")
        fun returnsMemberInfo() {
            val member = member()
            val accessor = testAccessor(Optional.of(member), Optional.of(CurrentMemberInfo.from(member)))

            val result = accessor.getMemberInfoOrThrow()

            assertThat(result.id()).isEqualTo(member.id)
        }

        @Test
        @DisplayName("현재 회원 정보가 없으면 예외가 발생한다")
        fun throwsWhenMissing() {
            val accessor = testAccessor(Optional.empty(), Optional.empty())

            ExceptionAssertions.assertApplicationException(
                accessor::getMemberInfoOrThrow,
                AuthErrorInfo.UNAUTHORIZED
            )
        }
    }

    private fun testAccessor(
        member: Optional<Member>,
        info: Optional<CurrentMemberInfo>
    ): CurrentMemberAccessor {
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfo(): Optional<CurrentMemberInfo> = info
            override fun getCurrentMember(): Optional<Member> = member
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
