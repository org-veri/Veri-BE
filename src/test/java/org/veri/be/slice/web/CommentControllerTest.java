package org.veri.be.slice.web;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
import org.veri.be.api.social.CommentController;
import org.veri.be.domain.comment.dto.request.CommentEditRequest;
import org.veri.be.domain.comment.dto.request.CommentPostRequest;
import org.veri.be.domain.comment.dto.request.ReplyPostRequest;
import org.veri.be.domain.comment.service.CommentCommandService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.global.auth.context.AuthenticatedMemberResolver;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.lib.response.ApiResponseAdvice;

@ExtendWith(MockitoExtension.class)
class CommentControllerTest {

    MockMvc mockMvc;
    ObjectMapper objectMapper;

    @Mock
    CommentCommandService commentCommandService;

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

        CommentController controller = new CommentController(commentCommandService);
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
    @DisplayName("POST /api/v1/comments")
    class PostComment {

        @Test
        @DisplayName("댓글을 작성하면 ID를 반환한다")
        void returnsCommentId() throws Exception {
            CommentPostRequest request = new CommentPostRequest(10L, "content");
            given(commentCommandService.postComment(eq(request), eq(member))).willReturn(100L);

            mockMvc.perform(post("/api/v1/comments")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value(100L));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments/reply")
    class PostReply {

        @Test
        @DisplayName("대댓글을 작성하면 ID를 반환한다")
        void returnsReplyId() throws Exception {
            ReplyPostRequest request = new ReplyPostRequest(20L, "reply");
            given(commentCommandService.postReply(20L, "reply", member)).willReturn(200L);

            mockMvc.perform(post("/api/v1/comments/reply")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.result").value(200L));
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/comments/{commentId}")
    class EditComment {

        @Test
        @DisplayName("댓글을 수정하면 서비스가 호출된다")
        void editsComment() throws Exception {
            CommentEditRequest request = new CommentEditRequest("edited");

            mockMvc.perform(patch("/api/v1/comments/30")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk());

            verify(commentCommandService).editComment(30L, "edited", member);
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/comments/{commentId}")
    class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제하면 서비스가 호출된다")
        void deletesComment() throws Exception {
            mockMvc.perform(delete("/api/v1/comments/40"))
                    .andExpect(status().isOk());

            verify(commentCommandService).deleteComment(40L, member);
        }
    }
}
