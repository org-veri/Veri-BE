package org.veri.be.global.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueTokenResponse {
    private String accessToken;
}
