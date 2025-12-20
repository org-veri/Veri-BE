package org.veri.be.slice.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.common.AuthController;
import org.veri.be.domain.auth.service.AuthService;
import org.veri.be.global.auth.dto.ReissueTokenRequest;
import org.veri.be.global.auth.dto.ReissueTokenResponse;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    MockMvc mockMvc;

    ObjectMapper objectMapper;

    @Mock
    AuthService authService;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        AuthController controller = new AuthController(authService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/auth/reissue")
    class ReissueToken {

        @Test
        @DisplayName("리프레시 토큰으로 새로운 액세스 토큰을 반환한다")
        void returnsNewAccessToken() throws Exception {
            ReissueTokenRequest request = new ReissueTokenRequest();
            ReflectionTestUtils.setField(request, "refreshToken", "refresh");
            ReissueTokenResponse response = ReissueTokenResponse.builder()
                    .accessToken("new-access")
                    .build();
            given(authService.reissueToken(any(ReissueTokenRequest.class))).willReturn(response);

            mockMvc.perform(post("/api/v1/auth/reissue")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.accessToken").value("new-access"));

            ArgumentCaptor<ReissueTokenRequest> requestCaptor = ArgumentCaptor.forClass(ReissueTokenRequest.class);
            verify(authService).reissueToken(requestCaptor.capture());
            assertThat(requestCaptor.getValue().getRefreshToken()).isEqualTo("refresh");
        }
    }

    @Nested
    @DisplayName("POST /api/v1/auth/logout")
    class Logout {

        @Test
        @DisplayName("로그아웃하면 상태 코드 204를 반환한다")
        void logsOut() throws Exception {
            mockMvc.perform(post("/api/v1/auth/logout")
                            .requestAttr("token", "access"))
                    .andExpect(status().isNoContent());

            verify(authService).logout("access");
        }
    }
}
