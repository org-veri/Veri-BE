package org.veri.be.domain.auth.service.oauth2;

import org.veri.be.domain.auth.dto.LoginResponse;

public interface OAuth2Service {
    LoginResponse login(String code, String origin);
}
