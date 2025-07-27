package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.entity.BlacklistedToken;
import org.goorm.veri.veribe.domain.auth.entity.RefreshToken;
import org.goorm.veri.veribe.domain.auth.repository.BlacklistedTokenRepository;
import org.goorm.veri.veribe.domain.auth.repository.RefreshTokenRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class DbTokenStorageService implements TokenStorageService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    @Override
    public void addRefreshToken(Long id, String refresh, long expiredAt) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(id)
                        .token(refresh)
                        .expiredAt(Instant.now().plusMillis(expiredAt))
                        .build()
        );
    }

    @Override
    public void addBlackList(String token, long expiredAt) {
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiredAt(Instant.now().plusMillis(expiredAt))
                        .build()
        );
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    @Override
    public boolean isBlackList(String token) {
        return blacklistedTokenRepository.findById(token)
                .map(b -> b.getExpiredAt().isAfter(Instant.now()))
                .orElse(false);
    }

    @Override
    public String getRefreshToken(Long id) {
        return refreshTokenRepository.findById(id)
                .filter(r -> r.getExpiredAt().isAfter(Instant.now()))
                .map(RefreshToken::getToken)
                .orElse(null);
    }
}
