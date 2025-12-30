package org.veri.be.global.auth;

public record JwtClaimsPayload(
        Long id,
        String email,
        String nickName,
        Boolean isAdmin
) {

    public static JwtClaimsPayload of(Long id, String email, String nickName, boolean isAdmin) {
        return new JwtClaimsPayload(id, email, nickName, isAdmin);
    }
}
