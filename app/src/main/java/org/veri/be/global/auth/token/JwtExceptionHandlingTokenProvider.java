package org.veri.be.global.auth.token;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import org.veri.be.global.auth.AuthErrorInfo;
import org.veri.be.lib.exception.http.UnAuthorizedException;

@RequiredArgsConstructor
public class JwtExceptionHandlingTokenProvider implements TokenProvider {

    private final TokenProvider delegate; // 에러 시스템과 인증 시스템을 분리하기 위함

    @Override
    public <T> TokenGeneration generateAccessToken(T claimsPayload) {
        return delegate.generateAccessToken(claimsPayload);
    }

    @Override
    public TokenGeneration generateRefreshToken(Long memberId) {
        return delegate.generateRefreshToken(memberId);
    }

    @Override
    public Claims parseAccessToken(String accessToken) {
        try {
            return delegate.parseAccessToken(accessToken);
        } catch (JwtException | IllegalArgumentException _) {
            throw new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED);
        }
    }

    @Override
    public Claims parseRefreshToken(String refreshToken) {
        try {
            return delegate.parseRefreshToken(refreshToken);
        } catch (JwtException | IllegalArgumentException _) {
            throw new UnAuthorizedException(AuthErrorInfo.UNAUTHORIZED);
        }
    }
}
