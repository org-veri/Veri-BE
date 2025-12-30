package org.veri.be.lib.auth.token;

public interface TokenBlacklistStore {

    void addBlackList(String token, long expiredAtMillis);

    boolean isBlackList(String token);
}
