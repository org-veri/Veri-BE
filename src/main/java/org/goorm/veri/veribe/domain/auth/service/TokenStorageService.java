package org.goorm.veri.veribe.domain.auth.service;

public interface TokenStorageService {
    void addBlackList(String token, long expiredAt);

    void addRefreshToken(Long id, String refresh, long expiredAt);

    void deleteRefreshToken(Long userId);

    boolean isBlackList(String token);

    String getRefreshToken(Long id);
}
