package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.OAuth2Request;
import org.goorm.veri.veribe.domain.auth.dto.OAuth2Response;

public interface AuthService {
    OAuth2Response.LoginResponse login(String provider, String code);
    OAuth2Response.ReissueTokenResponse reissueToken(OAuth2Request.AuthReissueRequest request);
}
