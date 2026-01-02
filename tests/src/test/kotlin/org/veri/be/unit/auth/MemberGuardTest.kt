package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThatCode
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.global.auth.guards.MemberGuard
import org.veri.be.support.assertion.ExceptionAssertions
import org.mockito.BDDMockito.given

@ExtendWith(MockitoExtension::class)
class MemberGuardTest {

    @Mock
    lateinit var currentMemberAccessor: CurrentMemberAccessor

    @InjectMocks
    lateinit var guard: MemberGuard

    @Nested
    @DisplayName("canActivate")
    inner class CanActivate {

        @Test
        @DisplayName("로그인한 사용자가 있으면 통과한다")
        fun allowsWhenMemberPresent() {
            given(currentMemberAccessor.getCurrentMemberInfoOrNull()).willReturn(
                CurrentMemberInfo.from(JwtClaimsPayload(member().id, member().email, member().nickname, false))
            )

            assertThatCode { guard.canActivate() }.doesNotThrowAnyException()
        }

        @Test
        @DisplayName("로그인한 사용자가 없으면 예외가 발생한다")
        fun throwsWhenMemberMissing() {
            given(currentMemberAccessor.getCurrentMemberInfoOrNull()).willReturn(null)

            ExceptionAssertions.assertApplicationException(
                guard::canActivate,
                CommonErrorCode.DOES_NOT_HAVE_PERMISSION
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
