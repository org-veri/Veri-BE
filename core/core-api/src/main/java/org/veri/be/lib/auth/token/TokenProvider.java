package org.veri.be.lib.auth.token;

import io.jsonwebtoken.Claims;

public interface TokenProvider {

    record TokenGeneration(String token, Long expiredAt) {}

    <T> TokenGeneration generateAccessToken(T claimsPayload);

    TokenGeneration generateRefreshToken(Long memberId);

    Claims parseAccessToken(String accessToken);

    Claims parseRefreshToken(String refreshToken);
}
