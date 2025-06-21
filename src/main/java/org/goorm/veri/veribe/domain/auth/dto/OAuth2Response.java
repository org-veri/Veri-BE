package org.goorm.veri.veribe.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class OAuth2Response {

    @Getter
    @Builder
    public class OAuth2LoginResponse {
        private Long id;
        private String accessToken;
        private String refreshToken;
    }
}
