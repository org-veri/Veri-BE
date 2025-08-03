package org.goorm.veri.veribe.global.jwt;


import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import org.goorm.veri.veribe.global.exception.CommonErrorInfo;
import org.goorm.veri.veribe.global.exception.http.UnAuthorizedException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtAuthenticator {

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtAuthenticator(
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.refresh.secret}") String refreshSecret
    ) {
        this.refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret.getBytes()));
        this.accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret.getBytes()));
    }

    public void verifyAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException | DeserializationException | MalformedJwtException e) {
            throw new UnAuthorizedException(CommonErrorInfo.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(CommonErrorInfo.EXPIRED_TOKEN);
        }
    }

    public void verifyRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (SignatureException | DeserializationException | MalformedJwtException e) {
            throw new UnAuthorizedException(CommonErrorInfo.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(CommonErrorInfo.EXPIRED_TOKEN);
        }

    }
}
