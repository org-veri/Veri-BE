package org.veri.be.global.auth;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public record JwtClaimsPayload(
        Long id,
        String email,
        String nickName,
        Boolean isAdmin
) {
}
