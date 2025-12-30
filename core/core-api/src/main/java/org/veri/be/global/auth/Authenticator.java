package org.veri.be.global.auth;

import org.veri.be.member.entity.Member;
import org.veri.be.global.auth.dto.LoginResponse;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.global.auth.oauth2.dto.OAuth2UserInfo;

public interface Authenticator {

    LoginResponse login(Member member);

    LoginResponse loginWithOAuth2(OAuth2UserInfo userInfo);

    ReissueTokenResponse reissueToken(ReissueTokenRequest request);

    void logout(String accessToken);
}
