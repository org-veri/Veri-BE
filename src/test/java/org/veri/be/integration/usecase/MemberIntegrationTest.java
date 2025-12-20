package org.veri.be.integration.usecase;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest;
import org.veri.be.integration.IntegrationTestSupport;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class MemberIntegrationTest extends IntegrationTestSupport {

    @Nested
    @DisplayName("GET /api/v1/members/me")
    class GetMe {
        @Test
        @DisplayName("정상 내 정보 조회")
        void getMeSuccess() throws Exception {
            mockMvc.perform(get("/api/v1/members/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.email").value(getMockMember().getEmail()));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    class UpdateInfo {
        @Test
        @DisplayName("닉네임/프로필 모두 수정")
        void updateInfoSuccess() throws Exception {
            UpdateMemberInfoRequest request = new UpdateMemberInfoRequest("newNick", "newUrl");

            mockMvc.perform(patch("/api/v1/members/me/info")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.nickname").value("newNick"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    class CheckNickname {
        @Test
        @DisplayName("닉네임 존재 여부 true")
        void existsTrue() throws Exception {
            mockMvc.perform(get("/api/v1/members/nickname/exists")
                            .param("nickname", getMockMember().getNickname()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(true));
        }

        @Test
        @DisplayName("파라미터 누락")
        void missingParam() throws Exception {
            mockMvc.perform(get("/api/v1/members/nickname/exists"))
                    .andExpect(status().isBadRequest());
        }
    }
}
