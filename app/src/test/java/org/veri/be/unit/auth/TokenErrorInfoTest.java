package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.lib.auth.jwt.TokenErrorInfo;

class TokenErrorInfoTest {

    @Nested
    @DisplayName("enum values")
    class EnumValues {

        @Test
        @DisplayName("토큰 에러 코드와 메시지가 존재한다")
        void hasMessageAndCode() {
            for (TokenErrorInfo info : TokenErrorInfo.values()) {
                assertThat(info.getMessage()).isNotBlank();
                assertThat(info.getCode()).isNotBlank();
            }
        }
    }
}
