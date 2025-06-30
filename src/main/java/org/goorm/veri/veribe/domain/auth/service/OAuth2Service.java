package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;

public interface OAuth2Service {
    AuthResponse.LoginResponse login(String code);
}
