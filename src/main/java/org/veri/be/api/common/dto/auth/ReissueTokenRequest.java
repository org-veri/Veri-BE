package org.veri.be.api.common.dto.auth;

import lombok.Getter;

@Getter
public class ReissueTokenRequest {
    private String refreshToken;
}
