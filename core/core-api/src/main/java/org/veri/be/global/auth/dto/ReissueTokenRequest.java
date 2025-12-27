package org.veri.be.global.auth.dto;

import lombok.Getter;
import jakarta.validation.constraints.NotBlank;

@Getter
public class ReissueTokenRequest {
    @NotBlank
    private String refreshToken;
}
