package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import io.jsonwebtoken.Claims;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.global.auth.JwtClaimsPayload;
import org.veri.be.lib.auth.jwt.JwtService;
import org.veri.be.lib.auth.jwt.data.JwtProperties;
import tools.jackson.databind.ObjectMapper;

class JwtServiceTest {

    JwtService jwtService;
    Clock fixedClock;

    @BeforeEach
    void setUp() {
        fixedClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneId.of("UTC"));
        JwtProperties properties = new JwtProperties();
        JwtProperties.TokenConfig access = new JwtProperties.TokenConfig();
        access.setSecret("AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA=");
        access.setValidity(3_600_000L);
        JwtProperties.TokenConfig refresh = new JwtProperties.TokenConfig();
        refresh.setSecret("AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE=");
        refresh.setValidity(1_200_000L);
        properties.setAccess(access);
        properties.setRefresh(refresh);

        jwtService = new JwtService(properties, fixedClock, new ObjectMapper());
    }

    @Nested
    @DisplayName("generateAccessToken/parseAccessToken")
    class AccessToken {

        @Test
        @DisplayName("클레임과 만료 시간이 포함된다")
        void generatesAccessTokenWithClaims() {
            JwtClaimsPayload payload = new JwtClaimsPayload(1L, "member@test.com", "member", false);

            long now = fixedClock.millis();
            var generated = jwtService.generateAccessToken(payload);
            Claims claims = jwtService.parseAccessToken(generated.token());

            assertThat(generated.expiredAt()).isEqualTo(now + 3_600_000L);
            assertThat(((Number) claims.get("id")).longValue()).isEqualTo(1L);
            assertThat(claims.get("email", String.class)).isEqualTo("member@test.com");
            assertThat(claims.get("nickName", String.class)).isEqualTo("member");
            assertThat(claims.get("isAdmin", Boolean.class)).isFalse();
        }
    }

    @Nested
    @DisplayName("generateRefreshToken/parseRefreshToken")
    class RefreshToken {

        @Test
        @DisplayName("멤버 ID를 포함한 리프레시 토큰을 생성한다")
        void generatesRefreshTokenWithMemberId() {
            long now = fixedClock.millis();
            var generated = jwtService.generateRefreshToken(2L);
            Claims claims = jwtService.parseRefreshToken(generated.token());

            assertThat(generated.expiredAt()).isEqualTo(now + 1_200_000L);
            assertThat(((Number) claims.get("id")).longValue()).isEqualTo(2L);
        }
    }
}
