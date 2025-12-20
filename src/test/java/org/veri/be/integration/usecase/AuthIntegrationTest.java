package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.veri.be.domain.auth.service.TokenStorageService;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.token.TokenProvider;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class AuthIntegrationTest extends IntegrationTestSupport {

    @Autowired TokenProvider tokenProvider;
    @Autowired TokenStorageService tokenStorageService;

    @Nested
    @DisplayName("POST /api/v1/auth/reissue")
    class Reissue {
        @Test
        @DisplayName("Stored refresh 토큰으로 재발급")
        void reissueSuccess() throws Exception {
            String refreshToken = tokenProvider.generateRefreshToken(getMockMember().getId()).token();
            tokenStorageService.addRefreshToken(getMockMember().getId(), refreshToken, 100000L);

            ReissueTokenRequest request = new ReissueTokenRequest();
            ReflectionTestUtils.setField(request, "refreshToken", refreshToken);

            mockMvc.perform(post("/api/v1/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.accessToken").exists());
        }

        @Test
        @DisplayName("만료 혹은 위조된 refresh 토큰")
        void invalidToken() throws Exception {
            ReissueTokenRequest request = new ReissueTokenRequest();
            ReflectionTestUtils.setField(request, "refreshToken", "invalid-token");

            mockMvc.perform(post("/api/v1/auth/reissue")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isUnauthorized()); 
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {
        @Test
        @DisplayName("정상 로그아웃")
        void logoutSuccess() throws Exception {
            String accessToken = tokenProvider.generateAccessToken(
                    org.veri.be.global.auth.JwtClaimsPayload.from(getMockMember())
            ).token();

            mockMvc.perform(post("/api/v1/auth/logout")
                            .requestAttr("token", accessToken))
                    .andExpect(status().isNoContent());
        }
    }
}
