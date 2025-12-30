package org.veri.be.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.auth.entity.BlacklistedToken;
import org.veri.be.auth.entity.RefreshToken;
import org.veri.be.auth.repository.BlacklistedTokenRepository;
import org.veri.be.auth.repository.RefreshTokenRepository;
import org.veri.be.lib.auth.token.TokenBlacklistStore;

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
