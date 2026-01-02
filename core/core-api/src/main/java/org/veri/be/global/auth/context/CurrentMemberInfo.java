package org.veri.be.global.auth.context;

import org.veri.be.global.auth.JwtClaimsPayload;

public record CurrentMemberInfo(
        Long id,
        String email,
        String nickname
) {
    public static CurrentMemberInfo from(JwtClaimsPayload member) {
        return new CurrentMemberInfo(
                member.id(),
                member.email(),
                member.nickName()
        );
    }
}
