package org.veri.be.global.jwt;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Base64;
import java.util.Date;

@Component
public class JwtProvider {

    private final String accessValidity;
    private final String refreshValidity;

    private final SecretKey accessKey;
    private final SecretKey refreshKey;

    public JwtProvider(
            @Value("${jwt.access.secret}") String accessSecret,
            @Value("${jwt.access.validity}") String accessValidity,
            @Value("${jwt.refresh.secret}") String refreshSecret,
            @Value("${jwt.refresh.validity}") String refreshValidity
    ) {
        this.refreshKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(refreshSecret.getBytes()));
        this.accessKey = Keys.hmacShaKeyFor(Base64.getDecoder().decode(accessSecret.getBytes()));

        this.accessValidity = accessValidity;
        this.refreshValidity = refreshValidity;
    }

    public String generateAccessToken(
            Long memberId,
            String nickName,
            boolean isAdmin
    ) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .claim(JwtClaim.ADMIN.getClaim(), isAdmin)
                .claim(JwtClaim.NICKNAME.getClaim(), nickName)
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + Long.parseLong(accessValidity)))
                .signWith(this.accessKey, Jwts.SIG.HS256)
                .compact();
    }

    public String generateRefreshToken(Long memberId) {
        return Jwts.builder()
                .subject(String.valueOf(memberId))
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(
                        System.currentTimeMillis() + Long.parseLong(refreshValidity)
                ))
                .signWith(refreshKey, Jwts.SIG.HS256)
                .compact();
    }
}
