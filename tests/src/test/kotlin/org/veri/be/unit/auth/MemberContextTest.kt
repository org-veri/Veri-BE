package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.lib.auth.context.MemberContext
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
        @DisplayName("저장된 멤버 ID를 반환한다")
        fun returnsStoredMemberId() {
            MemberContext.setCurrentMemberId(10L)

            assertThat(MemberContext.getCurrentMemberId()).contains(10L)
        }
    }

    @Nested
    @DisplayName("clear")
    inner class Clear {

        @Test
        @DisplayName("토큰과 ID를 모두 비운다")
        fun clearsContext() {
            MemberContext.setCurrentMemberId(1L)
            MemberContext.setCurrentToken("token")

            MemberContext.clear()

            assertThat(MemberContext.getCurrentMemberId()).isEmpty()
            assertThat(MemberContext.currentToken.get()).isNull()
        }
    }

    @Test
    @DisplayName("생성자는 외부에서 호출할 수 없도록 private이어야 한다")
    fun constructorShouldBePrivate() {
        val constructor: Constructor<MemberContext> = MemberContext::class.java.getDeclaredConstructor()

        assertThat(Modifier.isPrivate(constructor.modifiers)).isTrue()
    }
}
