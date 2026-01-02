package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.lib.auth.jwt.TokenErrorCode

class TokenErrorCodeTest {

    @Nested
    @DisplayName("enum values")
    inner class EnumValues {

        @Test
        @DisplayName("모든 에러 코드를 조회하면 → 메시지가 존재한다")
        fun hasMessageAndCode() {
            for (info in TokenErrorCode.values()) {
                assertThat(info.message).isNotBlank()
                assertThat(info.code).isNotBlank()
            }
        }
    }
}
