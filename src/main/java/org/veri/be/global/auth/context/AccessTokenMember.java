package org.veri.be.global.auth.context;


import io.jsonwebtoken.Claims;
import lombok.Builder;

import static org.veri.be.lib.auth.jwt.JwtClaim.*;

@Builder
public record AccessTokenMember(
        Long id,
        String email,
        String nickName,
        Boolean isAdmin
) {

    public static AccessTokenMember from(Claims accessTokenClaims) {
        Long memberId = Long.valueOf(accessTokenClaims.getSubject());
        String email = accessTokenClaims.get(EMAIL.getClaim(), String.class);
        String nickName = accessTokenClaims.get(NICKNAME.getClaim(), String.class);
        Boolean isAdmin = accessTokenClaims.get(ADMIN.getClaim(), Boolean.class);

        return AccessTokenMember.builder()
                .id(memberId)
                .email(email)
                .nickName(nickName)
                .isAdmin(isAdmin)
                .build();
    }
}
