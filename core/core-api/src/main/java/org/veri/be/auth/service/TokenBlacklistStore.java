package org.veri.be.auth.service;

public interface TokenBlacklistStore {

    void addBlackList(String token, long expiredAtMillis);

    boolean isBlackList(String token);
}
