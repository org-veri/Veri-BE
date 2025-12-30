package org.veri.be.unit.auth

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.auth.entity.BlacklistedToken
import org.veri.be.domain.auth.entity.RefreshToken
import org.veri.be.domain.auth.repository.BlacklistedTokenRepository
import org.veri.be.domain.auth.repository.RefreshTokenRepository
import org.veri.be.domain.auth.service.TokenStorageService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class TokenStorageServiceTest {

    @org.mockito.Mock
    private lateinit var refreshTokenRepository: RefreshTokenRepository

    @org.mockito.Mock
    private lateinit var blacklistedTokenRepository: BlacklistedTokenRepository

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

    private lateinit var tokenStorageService: TokenStorageService

    @org.mockito.Captor
    private lateinit var refreshTokenCaptor: ArgumentCaptor<RefreshToken>

    @org.mockito.Captor
    private lateinit var blacklistedTokenCaptor: ArgumentCaptor<BlacklistedToken>

    @BeforeEach
    fun setUp() {
        tokenStorageService = TokenStorageService(
            refreshTokenRepository,
            blacklistedTokenRepository,
            fixedClock
        )
    }

    @Nested
    @DisplayName("addRefreshToken")
    inner class AddRefreshToken {

        @Test
        @DisplayName("만료 시간을 더한 리프레시 토큰을 저장한다")
        fun savesRefreshTokenWithExpiry() {
            tokenStorageService.addRefreshToken(1L, "refresh-token", 1000L)

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture())
            val saved = refreshTokenCaptor.value

            assertThat(saved.userId).isEqualTo(1L)
            assertThat(saved.token).isEqualTo("refresh-token")
            assertThat(saved.expiredAt).isEqualTo(Instant.parse("2024-01-01T00:00:01Z"))
        }
    }

    @Nested
    @DisplayName("addBlackList")
    inner class AddBlackList {

        @Test
        @DisplayName("만료 시간을 더한 블랙리스트 토큰을 저장한다")
        fun savesBlacklistedTokenWithExpiry() {
            tokenStorageService.addBlackList("access-token", 2000L)

            verify(blacklistedTokenRepository).save(blacklistedTokenCaptor.capture())
            val saved = blacklistedTokenCaptor.value

            assertThat(saved.token).isEqualTo("access-token")
            assertThat(saved.expiredAt).isEqualTo(Instant.parse("2024-01-01T00:00:02Z"))
        }
    }

    @Nested
    @DisplayName("isBlackList")
    inner class IsBlackList {

        @Test
        @DisplayName("만료되지 않은 토큰은 블랙리스트로 판단한다")
        fun returnsTrueWhenNotExpired() {
            given(blacklistedTokenRepository.findById("token"))
                .willReturn(
                    Optional.of(
                        BlacklistedToken.builder()
                            .token("token")
                            .expiredAt(Instant.parse("2024-01-01T00:00:05Z"))
                            .build()
                    )
                )

            val result = tokenStorageService.isBlackList("token")

            assertThat(result).isTrue()
        }

        @Test
        @DisplayName("만료된 토큰은 블랙리스트로 판단하지 않는다")
        fun returnsFalseWhenExpired() {
            given(blacklistedTokenRepository.findById("token"))
                .willReturn(
                    Optional.of(
                        BlacklistedToken.builder()
                            .token("token")
                            .expiredAt(Instant.parse("2023-12-31T23:59:59Z"))
                            .build()
                    )
                )

            val result = tokenStorageService.isBlackList("token")

            assertThat(result).isFalse()
        }
    }

    @Nested
    @DisplayName("getRefreshToken")
    inner class GetRefreshToken {

        @Test
        @DisplayName("만료되지 않은 리프레시 토큰을 반환한다")
        fun returnsRefreshTokenWhenValid() {
            given(refreshTokenRepository.findById(1L))
                .willReturn(
                    Optional.of(
                        RefreshToken.builder()
                            .userId(1L)
                            .token("refresh-token")
                            .expiredAt(Instant.parse("2024-01-01T00:00:10Z"))
                            .build()
                    )
                )

            val token = tokenStorageService.getRefreshToken(1L)

            assertThat(token).isEqualTo("refresh-token")
        }

        @Test
        @DisplayName("만료된 리프레시 토큰은 null을 반환한다")
        fun returnsNullWhenExpired() {
            given(refreshTokenRepository.findById(1L))
                .willReturn(
                    Optional.of(
                        RefreshToken.builder()
                            .userId(1L)
                            .token("refresh-token")
                            .expiredAt(Instant.parse("2023-12-31T23:59:59Z"))
                            .build()
                    )
                )

            val token = tokenStorageService.getRefreshToken(1L)

            assertThat(token).isNull()
        }
    }

    @Nested
    @DisplayName("deleteRefreshToken")
    inner class DeleteRefreshToken {

        @Test
        @DisplayName("회원 ID로 리프레시 토큰을 삭제한다")
        fun deletesRefreshTokenById() {
            tokenStorageService.deleteRefreshToken(1L)

            verify(refreshTokenRepository).deleteById(1L)
        }
    }
}
