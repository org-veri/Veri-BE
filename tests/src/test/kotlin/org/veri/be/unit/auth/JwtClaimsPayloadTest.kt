package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.auth.JwtClaimsPayload

class JwtClaimsPayloadTest {

    @Nested
    @DisplayName("of")
    inner class Of {

        @Test
        @DisplayName("클레임 정보를 생성한다")
        fun mapsFieldsToClaims() {
            val payload = JwtClaimsPayload.of(1L, "member@test.com", "member", false)

            assertThat(payload.id()).isEqualTo(1L)
            assertThat(payload.email()).isEqualTo("member@test.com")
            assertThat(payload.nickName()).isEqualTo("member")
            assertThat(payload.isAdmin()).isFalse()
        }
    }
}
