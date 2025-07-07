package org.goorm.veri.veribe.global.util;

import lombok.RequiredArgsConstructor;

import org.springframework.context.annotation.Profile;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Profile("!dev")
@Component
@RequiredArgsConstructor
public class RedisUtil {

    private final RedisTemplate<String, Object> redisTokenTemplate;

    public void save(String key, Object value, Duration duration) {
        redisTokenTemplate.opsForValue().set(key, value, duration);
    }

    public boolean hasKey(String key) {
        return Boolean.TRUE.equals(redisTokenTemplate.hasKey(key));
    }

    public Object get(String key) {
        return redisTokenTemplate.opsForValue().get(key);
    }

    public void delete(String key) {
        redisTokenTemplate.delete(key);
    }

}
