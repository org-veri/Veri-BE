package org.veri.be.domain.auth.service;

public interface TokenBlacklistStore {

    void addBlackList(String token, long expiredAtMillis);

    boolean isBlackList(String token);
}
