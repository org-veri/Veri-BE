package org.goorm.veri.veribe.global.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.goorm.veri.veribe.domain.auth.exception.TokenErrorCode;
import org.goorm.veri.veribe.domain.auth.exception.TokenException;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.data.JwtConfigData;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration accessExpiration;
    private final Duration refreshExpiration;

    public JwtUtil(JwtConfigData jwtConfigData) {
        this.secretKey = Keys.hmacShaKeyFor(jwtConfigData.getSecret().getBytes(StandardCharsets.UTF_8));
        this.accessExpiration = Duration.ofMillis(jwtConfigData.getTime().getAccess());
        this.refreshExpiration = Duration.ofMillis(jwtConfigData.getTime().getRefresh());
    }

    public String createAccessToken(Member member) {
        return createToken(member, accessExpiration);
    }

    public String createRefreshToken(Member member) {
        return createToken(member, refreshExpiration);
    }

    public Long getUserId(String token) {
        try {
            return getClaims(token).getPayload().get("id", Long.class);
        } catch (JwtException e) {
            return null;
        }
    }

    public boolean isValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            throw new TokenException(TokenErrorCode.TOKEN_EXPIRED);
        } catch (JwtException e) {
            return false;
        }
    }

    private String createToken(Member member, Duration expiration) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(member.getEmail())
                .claim("id", member.getId())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plus(expiration)))
                .signWith(secretKey)
                .compact();
    }

    private Jws<Claims> getClaims(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(secretKey)
                .clockSkewSeconds(60)
                .build()
                .parseSignedClaims(token);
    }
}
