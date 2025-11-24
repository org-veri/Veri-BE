package org.veri.be.global.auth.dto;

import lombok.Getter;

@Getter
public class ReissueTokenRequest {
    private String refreshToken;
}
