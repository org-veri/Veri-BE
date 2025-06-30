package org.goorm.veri.veribe.domain.auth.service;

import org.goorm.veri.veribe.domain.auth.dto.AuthRequest;
import org.goorm.veri.veribe.domain.auth.dto.AuthResponse;

public interface AuthService {
    AuthResponse.LoginResponse login(String provider, String code);
    AuthResponse.ReissueTokenResponse reissueToken(AuthRequest.AuthReissueRequest request);
}
