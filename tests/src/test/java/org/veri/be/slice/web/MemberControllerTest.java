package org.veri.be.slice.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.veri.be.api.personal.MemberController;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.service.MemberCommandService;
import org.veri.be.domain.member.service.MemberQueryService;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    MemberCommandService memberCommandService;

    @Mock
    MemberQueryService memberQueryService;

    Member member;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper().findAndRegisterModules();
        member = Member.builder()
                .id(1L)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
        MemberContext.setCurrentMember(member);

        MemberController controller = new MemberController(memberCommandService, memberQueryService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setControllerAdvice(new ApiResponseAdvice())
                .setCustomArgumentResolvers(new AuthenticatedMemberResolver(new ThreadLocalCurrentMemberAccessor(null)))
                .build();
    }

    @AfterEach
    void tearDown() {
        MemberContext.clear();
    }

    @Nested
    @DisplayName("GET /api/v1/members/me")
    class GetMyInfo {

        @Test
        @DisplayName("내 정보를 반환한다")
        void returnsMyInfo() throws Exception {
            MemberResponse.MemberInfoResponse response = MemberResponse.MemberInfoResponse.builder()
                    .email("member@test.com")
                    .nickname("member")
                    .image("https://example.com/profile.png")
                    .numOfReadBook(3)
                    .numOfCard(5)
                    .build();
            given(memberQueryService.findMyInfo(member)).willReturn(response);

            mockMvc.perform(get("/api/v1/members/me"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.nickname").value("member"));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/members/me/info")
    class UpdateInfo {

        @Test
        @DisplayName("내 정보를 수정하면 결과를 반환한다")
        void updatesInfo() throws Exception {
            UpdateMemberInfoRequest request = new UpdateMemberInfoRequest(
                    "new-nickname",
                    "https://example.com/new.png"
            );
            MemberResponse.MemberSimpleResponse response = MemberResponse.MemberSimpleResponse.builder()
                    .id(1L)
                    .nickname("new-nickname")
                    .image("https://example.com/new.png")
                    .build();
            given(memberCommandService.updateInfo(eq(request), eq(member))).willReturn(response);

            mockMvc.perform(patch("/api/v1/members/me/info")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result.nickname").value("new-nickname"));
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        void returns400WhenFieldMissing() throws Exception {
            UpdateMemberInfoRequest request = new UpdateMemberInfoRequest(null, null);

            mockMvc.perform(patch("/api/v1/members/me/info")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/members/nickname/exists")
    class CheckNicknameExists {

        @Test
        @DisplayName("닉네임 중복 여부를 반환한다")
        void returnsExists() throws Exception {
            given(memberQueryService.existsByNickname("member")).willReturn(true);

            mockMvc.perform(get("/api/v1/members/nickname/exists")
                            .param("nickname", "member"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.result").value(true));

            verify(memberQueryService).existsByNickname("member");
        }
    }
}
