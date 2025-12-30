package org.veri.be.global.auth.token;

public interface TokenBlacklistStore {

    void addBlackList(String token, long expiredAtMillis);

    boolean isBlackList(String token);
}
