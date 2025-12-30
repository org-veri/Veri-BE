package org.veri.be.global.auth;


import org.veri.be.member.entity.Member;

public record JwtClaimsPayload(
        Long id,
        String email,
        String nickName,
        Boolean isAdmin
) {

    public static JwtClaimsPayload from(Member member) {
        return new JwtClaimsPayload(member.getId(), member.getEmail(), member.getNickname(), false);
    }
}
