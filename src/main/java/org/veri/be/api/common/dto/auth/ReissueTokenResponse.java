package org.veri.be.api.common.dto.auth;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ReissueTokenResponse {
    private String accessToken;
}
