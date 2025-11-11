package org.veri.be.domain.auth.service.token;

public interface TokenStorageService {
    void addBlackList(String token, long expiredAt);

    void addRefreshToken(Long id, String refresh, long expiredAt);

    void deleteRefreshToken(Long userId);

    boolean isBlackList(String token);

    String getRefreshToken(Long id);
}
