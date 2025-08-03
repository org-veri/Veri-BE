package org.goorm.veri.veribe.global.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;

@Component
public class JwtExtractor {

  private final SecretKey accessKey;
  private final SecretKey refreshKey;

  public JwtExtractor(
      @Value("${jwt.access.secret}") String accessSecret,
      @Value("${jwt.refresh.secret}") String refreshSecret
  ) {
    this.refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret.getBytes()));
    this.accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret.getBytes()));
  }

  public Claims parseAccessTokenPayloads(String accessToken) {
    return Jwts.parser()
        .verifyWith(accessKey)
        .build()
        .parseSignedClaims(accessToken)
        .getPayload();
  }

  public Claims parseRefreshTokenPayloads(String refreshToken) {
    return Jwts.parser()
        .verifyWith(refreshKey)
        .build()
        .parseSignedClaims(refreshToken)
        .getPayload();
  }
}
