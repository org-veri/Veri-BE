package org.veri.be.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.auth.entity.BlacklistedToken;
import org.veri.be.domain.auth.entity.RefreshToken;
import org.veri.be.domain.auth.repository.BlacklistedTokenRepository;
import org.veri.be.domain.auth.repository.RefreshTokenRepository;
import org.veri.be.domain.auth.service.TokenStorageService;

@ExtendWith(MockitoExtension.class)
class TokenStorageServiceTest {

    @Mock
    RefreshTokenRepository refreshTokenRepository;

    @Mock
    BlacklistedTokenRepository blacklistedTokenRepository;

    Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));

    TokenStorageService tokenStorageService;

    @BeforeEach
    void setUp() {
        tokenStorageService = new TokenStorageService(
                refreshTokenRepository,
                blacklistedTokenRepository,
                fixedClock
        );
    }

    @Captor
    ArgumentCaptor<RefreshToken> refreshTokenCaptor;

    @Captor
    ArgumentCaptor<BlacklistedToken> blacklistedTokenCaptor;

    @Nested
    @DisplayName("addRefreshToken")
    class AddRefreshToken {

        @Test
        @DisplayName("만료 시간을 더한 리프레시 토큰을 저장한다")
        void savesRefreshTokenWithExpiry() {
            tokenStorageService.addRefreshToken(1L, "refresh-token", 1000L);

            verify(refreshTokenRepository).save(refreshTokenCaptor.capture());
            RefreshToken saved = refreshTokenCaptor.getValue();

            assertThat(saved.getUserId()).isEqualTo(1L);
            assertThat(saved.getToken()).isEqualTo("refresh-token");
            assertThat(saved.getExpiredAt()).isEqualTo(Instant.parse("2024-01-01T00:00:01Z"));
        }
    }

    @Nested
    @DisplayName("addBlackList")
    class AddBlackList {

        @Test
        @DisplayName("만료 시간을 더한 블랙리스트 토큰을 저장한다")
        void savesBlacklistedTokenWithExpiry() {
            tokenStorageService.addBlackList("access-token", 2000L);

            verify(blacklistedTokenRepository).save(blacklistedTokenCaptor.capture());
            BlacklistedToken saved = blacklistedTokenCaptor.getValue();

            assertThat(saved.getToken()).isEqualTo("access-token");
            assertThat(saved.getExpiredAt()).isEqualTo(Instant.parse("2024-01-01T00:00:02Z"));
        }
    }

    @Nested
    @DisplayName("isBlackList")
    class IsBlackList {

        @Test
        @DisplayName("만료되지 않은 토큰은 블랙리스트로 판단한다")
        void returnsTrueWhenNotExpired() {
            given(blacklistedTokenRepository.findById(eq("token")))
                    .willReturn(Optional.of(BlacklistedToken.builder()
                            .token("token")
                            .expiredAt(Instant.parse("2024-01-01T00:00:05Z"))
                            .build()));

            boolean result = tokenStorageService.isBlackList("token");

            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("만료된 토큰은 블랙리스트로 판단하지 않는다")
        void returnsFalseWhenExpired() {
            given(blacklistedTokenRepository.findById(eq("token")))
                    .willReturn(Optional.of(BlacklistedToken.builder()
                            .token("token")
                            .expiredAt(Instant.parse("2023-12-31T23:59:59Z"))
                            .build()));

            boolean result = tokenStorageService.isBlackList("token");

            assertThat(result).isFalse();
        }
    }

    @Nested
    @DisplayName("getRefreshToken")
    class GetRefreshToken {

        @Test
        @DisplayName("만료되지 않은 리프레시 토큰을 반환한다")
        void returnsRefreshTokenWhenValid() {
            given(refreshTokenRepository.findById(eq(1L)))
                    .willReturn(Optional.of(RefreshToken.builder()
                            .userId(1L)
                            .token("refresh-token")
                            .expiredAt(Instant.parse("2024-01-01T00:00:10Z"))
                            .build()));

            String token = tokenStorageService.getRefreshToken(1L);

            assertThat(token).isEqualTo("refresh-token");
        }

        @Test
        @DisplayName("만료된 리프레시 토큰은 null을 반환한다")
        void returnsNullWhenExpired() {
            given(refreshTokenRepository.findById(eq(1L)))
                    .willReturn(Optional.of(RefreshToken.builder()
                            .userId(1L)
                            .token("refresh-token")
                            .expiredAt(Instant.parse("2023-12-31T23:59:59Z"))
                            .build()));

            String token = tokenStorageService.getRefreshToken(1L);

            assertThat(token).isNull();
        }
    }

    @Nested
    @DisplayName("deleteRefreshToken")
    class DeleteRefreshToken {

        @Test
        @DisplayName("회원 ID로 리프레시 토큰을 삭제한다")
        void deletesRefreshTokenById() {
            tokenStorageService.deleteRefreshToken(1L);

            verify(refreshTokenRepository).deleteById(1L);
        }
    }
}
