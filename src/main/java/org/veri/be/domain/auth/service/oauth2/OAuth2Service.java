package org.veri.be.domain.auth.service.oauth2;

import org.veri.be.domain.auth.dto.AuthResponse;

public interface OAuth2Service {
    AuthResponse.LoginResponse login(String code, String origin);
}
