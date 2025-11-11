package org.veri.be.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueTokenResponse {
    private String accessToken;
}
