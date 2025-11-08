package org.veri.be.lib.auth.jwt;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.io.DeserializationException;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.veri.be.lib.auth.jwt.data.JwtProperties;
import org.veri.be.lib.exception.http.UnAuthorizedException;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;
import java.util.Map;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class JwtUtil {

    private static SecretKey accessKey;
    private static Long accessValidity;

    private static SecretKey refreshKey;
    private static Long refreshValidity;

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static void init(
            JwtProperties properties
    ) {
        if (accessKey != null || accessValidity != null || refreshKey != null || refreshValidity != null) {
            return;
        }

        accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getAccess().getSecret().getBytes()));
        accessValidity = properties.getAccess().getValidity();

        refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(properties.getRefresh().getSecret().getBytes()));
        refreshValidity = properties.getRefresh().getValidity();
    }

    public static <T> String generateAccessToken(T claimsPayload) {
        Map<String, Object> claims = objectMapper.convertValue(
                claimsPayload,
                new TypeReference<>() {
                }
        );

        return Jwts.builder()
                .subject("veri")
                .claims(claims)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + accessValidity))
                .signWith(accessKey, Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseAccessTokenPayloads(String accessToken) {
        return Jwts.parser()
                .verifyWith(accessKey)
                .build()
                .parseSignedClaims(accessToken)
                .getPayload();
    }

    public static <T> String generateRefreshToken(T memberId) {
        return Jwts.builder()
                .subject("veri")
                .claim("id", memberId)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + refreshValidity))
                .signWith(refreshKey, Jwts.SIG.HS256)
                .compact();
    }

    public static Claims parseRefreshTokenPayloads(String refreshToken) {
        return Jwts.parser()
                .verifyWith(refreshKey)
                .build()
                .parseSignedClaims(refreshToken)
                .getPayload();
    }

    public static void verifyAccessToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(accessKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (SignatureException | DeserializationException | MalformedJwtException e) {
            throw new UnAuthorizedException(TokenErrorInfo.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(TokenErrorInfo.EXPIRED_TOKEN);
        }
    }

    public static void verifyRefreshToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(refreshKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();

        } catch (SignatureException | DeserializationException | MalformedJwtException e) {
            throw new UnAuthorizedException(TokenErrorInfo.INVALID_TOKEN);
        } catch (ExpiredJwtException e) {
            throw new UnAuthorizedException(TokenErrorInfo.EXPIRED_TOKEN);
        }
    }
}
