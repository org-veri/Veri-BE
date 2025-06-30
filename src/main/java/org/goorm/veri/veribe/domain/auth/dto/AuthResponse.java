package org.goorm.veri.veribe.domain.auth.dto;

import lombok.Builder;
import lombok.Getter;

public class AuthResponse {

    @Getter
    @Builder
    public static class LoginResponse {
        private String accessToken;
        private String refreshToken;
    }

    @Getter
    @Builder
    public static class ReissueTokenResponse {
        private String accessToken;
    }
}
