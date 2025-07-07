package org.goorm.veri.veribe.mock;

import org.goorm.veri.veribe.domain.auth.service.TokenStorageService;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("dev")
public class MockTokenStorageService implements TokenStorageService {
    private final Map<String, Object> storage = new ConcurrentHashMap<>();

    @Override
    public void addRefreshToken(Long id, String refresh) {
        storage.put("REFRESH:" + id, refresh);
    }

    @Override
    public void addBlackList(String token) {
        storage.put("BLACK:" + token, true);
    }

    @Override
    public void deleteRefreshToken(Long userId) {
        storage.remove("REFRESH:" + userId);
    }

    @Override
    public boolean isBlackList(String token) {
        return Boolean.TRUE.equals(storage.get("BLACK:" + token));
    }

    @Override
    public String getRefreshToken(Long id) {
        return (String) storage.get("REFRESH:" + id);
    }
} 