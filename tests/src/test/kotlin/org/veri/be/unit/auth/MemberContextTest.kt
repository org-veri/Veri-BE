package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.MemberContext
import java.lang.reflect.Constructor
import java.lang.reflect.Modifier

class MemberContextTest {

    @AfterEach
    fun tearDown() {
        MemberContext.clear()
    }

    @Nested
    @DisplayName("getCurrentMemberId")
    inner class GetCurrentMemberId {

        @Test
        @DisplayName("멤버 정보를 저장하면 → ID를 반환한다")
        fun returnsStoredMemberId() {
            MemberContext.setCurrentMemberInfo(JwtClaimsPayload(10L, "member@test.com", "member", false))

            assertThat(MemberContext.getCurrentMemberId()).contains(10L)
        }
    }

    @Nested
    @DisplayName("clear")
    inner class Clear {

        @Test
        @DisplayName("clear를 호출하면 → 토큰과 ID를 모두 비운다")
        fun clearsContext() {
            MemberContext.setCurrentMemberInfo(JwtClaimsPayload(1L, "member@test.com", "member", false))
            MemberContext.setCurrentToken("token")

            MemberContext.clear()

            assertThat(MemberContext.getCurrentMemberId()).isEmpty()
            assertThat(MemberContext.currentToken.get()).isNull()
        }
    }

    @Test
    @DisplayName("생성자를 조회하면 → private으로 제한된다")
    fun constructorShouldBePrivate() {
        val constructor: Constructor<MemberContext> = MemberContext::class.java.getDeclaredConstructor()

        assertThat(Modifier.isPrivate(constructor.modifiers)).isTrue()
    }
}
