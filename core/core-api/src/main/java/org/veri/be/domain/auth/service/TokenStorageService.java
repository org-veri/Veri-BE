package org.veri.be.domain.auth.service;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.auth.entity.BlacklistedToken;
import org.veri.be.domain.auth.entity.RefreshToken;
import org.veri.be.domain.auth.repository.BlacklistedTokenRepository;
import org.veri.be.domain.auth.repository.RefreshTokenRepository;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenStorageService implements TokenBlacklistStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final Clock clock;

    // 네거티브 캐시: 블랙리스트에 없는 토큰도 캐싱하여 DB 조회 감소
    private final Cache<String, Boolean> blacklistCache = Caffeine.newBuilder()
            .expireAfterWrite(Duration.ofSeconds(30))
            .maximumSize(1000)
            .build();

    public void addRefreshToken(Long id, String refresh, long expiredAt) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(id)
                        .token(refresh)
                        .expiredAt(Instant.now(clock).plusMillis(expiredAt))
                        .build()
        );
    }

    @Override
    public void addBlackList(String token, long expiredAt) {
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiredAt(Instant.now(clock).plusMillis(expiredAt))
                        .build()
        );
        // 캐시도 즉시 업데이트
        blacklistCache.put(token, true);
    }

    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    @Override
    public boolean isBlackList(String token) {
        if (token == null) {
            return false;
        }

        // 1차: 캐시에서 조회
        Boolean cached = blacklistCache.getIfPresent(token);
        if (cached != null) {
            return cached;
        }

        // 2차: DB 조회 후 캐시에 저장
        boolean isBlacklisted = blacklistedTokenRepository.findById(token)
                .map(b -> b.getExpiredAt().isAfter(Instant.now(clock)))
                .orElse(false);
        blacklistCache.put(token, isBlacklisted);
        return isBlacklisted;
    }

    public String getRefreshToken(Long id) {
        return refreshTokenRepository.findById(id)
                .filter(r -> r.getExpiredAt().isAfter(Instant.now(clock)))
                .map(RefreshToken::getToken)
                .orElse(null);
    }
}
