package org.goorm.veri.veribe.domain.auth.service;

public interface TokenStorageService {
    void addRefreshToken(Long id, String refresh);
    void addBlackList(String token);
    void deleteRefreshToken(Long userId);
    boolean isBlackList(String token);
    String getRefreshToken(Long id);
}
