package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;

public interface OAuth2Service {
    OAuth2Response.LoginResponse login(String code);
}
