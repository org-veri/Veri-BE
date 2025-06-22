package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;

public interface AuthService {
    OAuth2Response.OAuth2LoginResponse login(String provider, String code);
}
