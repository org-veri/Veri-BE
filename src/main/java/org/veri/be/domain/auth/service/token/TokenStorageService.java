package org.veri.be.domain.auth.service.token;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.veri.be.domain.auth.entity.BlacklistedToken;
import org.veri.be.domain.auth.entity.RefreshToken;
import org.veri.be.domain.auth.repository.BlacklistedTokenRepository;
import org.veri.be.domain.auth.repository.RefreshTokenRepository;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class TokenStorageService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final BlacklistedTokenRepository blacklistedTokenRepository;

    public void addRefreshToken(Long id, String refresh, long expiredAt) {
        refreshTokenRepository.save(
                RefreshToken.builder()
                        .userId(id)
                        .token(refresh)
                        .expiredAt(Instant.now().plusMillis(expiredAt))
                        .build()
        );
    }

    public void addBlackList(String token, long expiredAt) {
        blacklistedTokenRepository.save(
                BlacklistedToken.builder()
                        .token(token)
                        .expiredAt(Instant.now().plusMillis(expiredAt))
                        .build()
        );
    }

    public void deleteRefreshToken(Long userId) {
        refreshTokenRepository.deleteById(userId);
    }

    public boolean isBlackList(String token) {
        return blacklistedTokenRepository.findById(token)
                .map(b -> b.getExpiredAt().isAfter(Instant.now()))
                .orElse(false);
    }

    public String getRefreshToken(Long id) {
        return refreshTokenRepository.findById(id)
                .filter(r -> r.getExpiredAt().isAfter(Instant.now()))
                .map(RefreshToken::getToken)
                .orElse(null);
    }
}
