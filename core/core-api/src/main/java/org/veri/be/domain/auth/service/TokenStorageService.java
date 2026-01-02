package org.veri.be.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.veri.be.domain.auth.entity.BlacklistedToken;
import org.veri.be.domain.auth.entity.RefreshToken;
import org.veri.be.domain.auth.repository.BlacklistedTokenRepository;
import org.veri.be.domain.auth.repository.RefreshTokenRepository;
import org.veri.be.global.cache.CacheConfig;

import java.time.Clock;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenStorageService implements TokenBlacklistStore {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;
    private final Clock clock;

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
    @CacheEvict(cacheNames = CacheConfig.TOKEN_BLACKLIST, key = "#token", condition = "#token != null")
    public void addBlackList(String token, long expiredAt) {
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiredAt(Instant.now(clock).plusMillis(expiredAt))
                        .build()
        );
    }

    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    @Override
    @Cacheable(cacheNames = CacheConfig.TOKEN_BLACKLIST, key = "#token", condition = "#token != null")
    public boolean isBlackList(String token) {
        if (token == null) {
            return false;
        }

        return blacklistedTokenRepository.findById(token)
                .map(b -> b.getExpiredAt().isAfter(Instant.now(clock)))
                .orElse(false);
    }

    public String getRefreshToken(Long id) {
        return refreshTokenRepository.findById(id)
                .filter(r -> r.getExpiredAt().isAfter(Instant.now(clock)))
                .map(RefreshToken::getToken)
                .orElse(null);
    }
}
