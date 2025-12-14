package org.veri.be.lib.auth.jwt;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.veri.be.global.auth.token.TokenProvider;
import org.veri.be.lib.auth.jwt.data.JwtProperties;
import tools.jackson.databind.ObjectMapper;

import javax.crypto.SecretKey;
import java.time.Clock;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@Component
public class JwtService implements TokenProvider {

    private final SecretKey accessKey;
    private final long accessValidity;
    private final SecretKey refreshKey;
    private final long refreshValidity;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public JwtService(JwtProperties properties, Clock clock, ObjectMapper objectMapper) {
        this.accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getAccess().getSecret().getBytes()));
        this.accessValidity = properties.getAccess().getValidity();
        this.refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getRefresh().getSecret().getBytes()));
        this.refreshValidity = properties.getRefresh().getValidity();
        this.clock = clock;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> TokenGeneration generateAccessToken(T claimsPayload) {
        Map<String, Object> claims = objectMapper.convertValue(claimsPayload, Map.class);

        long now = clock.millis();
        long expiration = now + accessValidity;

        String token = Jwts.builder()
                .subject("veri")
                .claims(claims)
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();

        return new TokenGeneration(token, expiration);
    }

    @Override
    public Claims parseAccessToken(String accessToken) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    @Override
    public TokenGeneration generateRefreshToken(Long memberId) {
        long now = clock.millis();
        long expiration = now + refreshValidity;

        String token = Jwts.builder()
                .subject("veri")
                .claim("id", memberId)
                .issuedAt(new Date(now))
                .expiration(new Date(expiration))
                .signWith(refreshKey, Jwts.SIG.HS256)
                .compact();

        return new TokenGeneration(token, expiration);
    }

    @Override
    public Claims parseRefreshToken(String refreshToken) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
    }
}
