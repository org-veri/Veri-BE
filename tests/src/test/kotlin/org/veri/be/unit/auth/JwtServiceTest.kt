package org.veri.be.unit.auth

import io.jsonwebtoken.Claims
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.lib.auth.jwt.JwtService
import org.veri.be.lib.auth.jwt.data.JwtProperties
import tools.jackson.databind.ObjectMapper
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class JwtServiceTest {

    private lateinit var jwtService: JwtService
    private lateinit var fixedClock: Clock

    @BeforeEach
    fun setUp() {
        fixedClock = Clock.fixed(Instant.parse("2030-01-01T00:00:00Z"), ZoneId.of("UTC"))
        val properties = JwtProperties()
        val access = JwtProperties.TokenConfig()
        access.secret = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA="
        access.validity = 3_600_000L
        val refresh = JwtProperties.TokenConfig()
        refresh.secret = "AQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQEBAQE="
        refresh.validity = 1_200_000L
        properties.access = access
        properties.refresh = refresh

        jwtService = JwtService(properties, fixedClock, ObjectMapper())
    }

    @Nested
    @DisplayName("generateAccessToken/parseAccessToken")
    inner class AccessToken {

        @Test
        @DisplayName("토큰을 생성하면 → 클레임과 만료 시간이 포함된다")
        fun generatesAccessTokenWithClaims() {
            val payload = JwtClaimsPayload(1L, "member@test.com", "member", false)

            val now = fixedClock.millis()
            val generated = jwtService.generateAccessToken(payload)
            val claims: Claims = jwtService.parseAccessToken(generated.token())

            assertThat(generated.expiredAt()).isEqualTo(now + 3_600_000L)
            assertThat((claims["id"] as Number).toLong()).isEqualTo(1L)
            assertThat(claims.get("email", String::class.java)).isEqualTo("member@test.com")
            assertThat(claims.get("nickName", String::class.java)).isEqualTo("member")
            val isAdminValue = claims["isAdmin"] ?: claims["admin"]
            assertThat(isAdminValue?.toString()).isEqualTo("false")
        }
    }

    @Nested
    @DisplayName("generateRefreshToken/parseRefreshToken")
    inner class RefreshToken {

        @Test
        @DisplayName("리프레시 토큰을 생성하면 → 멤버 ID가 포함된다")
        fun generatesRefreshTokenWithMemberId() {
            val now = fixedClock.millis()
            val generated = jwtService.generateRefreshToken(2L)
            val claims: Claims = jwtService.parseRefreshToken(generated.token())

            assertThat(generated.expiredAt()).isEqualTo(now + 1_200_000L)
            assertThat((claims["id"] as Number).toLong()).isEqualTo(2L)
        }
    }
}
