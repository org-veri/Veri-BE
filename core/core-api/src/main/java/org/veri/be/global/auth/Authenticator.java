package org.veri.be.global.auth;

import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;

public interface Authenticator {

    LoginResponse login(JwtClaimsPayload claimsPayload);

    LoginResponse loginWithOAuth2(OAuth2UserInfo userInfo);

    ReissueTokenResponse reissueToken(ReissueTokenRequest request);

    void logout(String accessToken);
}
