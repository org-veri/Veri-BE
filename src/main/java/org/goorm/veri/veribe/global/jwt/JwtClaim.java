package org.goorm.veri.veribe.global.jwt;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum JwtClaim {

    // AccessToken
    ID("id"),
    EMAIL("email"),
    ADMIN("admin"),
    NICKNAME("nickname"),
    ;

    private final String claim;
}
