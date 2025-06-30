package org.goorm.veri.veribe.domain.auth.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.global.data.JwtConfigData;
import org.goorm.veri.veribe.global.util.RedisUtil;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisStorageService implements TokenStorageService {


    public static final String BLACKLIST_PREFIX = "BLACK:";
    public static final String REFRESH_TOKEN_PREFIX = "REFRESH:";

    private final RedisUtil redisUtil;
    private final JwtConfigData jwtConfigData;

    @Override
    public void addRefreshToken(Long id, String refresh) {
        redisUtil.save(REFRESH_TOKEN_PREFIX + id, refresh, Duration.ofMillis(jwtConfigData.getTime().getRefresh()));
    }

    @Override
    public void addBlackList(String token) {
        redisUtil.save(BLACKLIST_PREFIX + token, true, Duration.ofMillis(jwtConfigData.getTime().getAccess()));
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        redisUtil.delete(REFRESH_TOKEN_PREFIX + userId);
    }

    @Override
    public boolean isBlackList(String token) {
        return Boolean.TRUE.equals(redisUtil.hasKey(BLACKLIST_PREFIX + token));
    }

    @Override
    public String getRefreshToken(Long id) {
        return (String) redisUtil.get(REFRESH_TOKEN_PREFIX + id);
    }

}
