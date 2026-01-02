package org.veri.be.slice.web

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import org.veri.be.api.social.CommentController
import org.veri.be.domain.comment.dto.request.CommentEditRequest
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.dto.request.ReplyPostRequest
import org.veri.be.domain.comment.service.CommentCommandService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.lib.response.ApiResponseAdvice
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CommentControllerTest {

    private lateinit var mockMvc: MockMvc
    private lateinit var objectMapper: ObjectMapper

    @org.mockito.Mock
    private lateinit var commentCommandService: CommentCommandService

    private lateinit var member: Member

    @BeforeEach
    fun setUp() {
        objectMapper = ObjectMapper().findAndRegisterModules()
        member = Member.builder()
            .id(1L)
            .email("member@test.com")
            .nickname("member")
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-1")
            .providerType(ProviderType.KAKAO)
            .build()

        val controller = CommentController(commentCommandService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(member))
            )
            .build()
    }

    private fun testMemberAccessor(member: Member): CurrentMemberAccessor {
        val info = CurrentMemberInfo.from(member)
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfoOrNull() = info
            override fun getCurrentMember() = Optional.of(member)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments")
    inner class PostComment {

        @Test
        @DisplayName("댓글을 작성하면 ID를 반환한다")
        fun returnsCommentId() {
            val request = CommentPostRequest(10L, "content")
            given(commentCommandService.postComment(request, member)).willReturn(100L)

            mockMvc.perform(
                post("/api/v1/comments")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").value(100L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = CommentPostRequest(null, null)

            mockMvc.perform(
                post("/api/v1/comments")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments/reply")
    inner class PostReply {

        @Test
        @DisplayName("대댓글을 작성하면 ID를 반환한다")
        fun returnsReplyId() {
            val request = ReplyPostRequest(20L, "reply")
            given(commentCommandService.postReply(20L, "reply", member)).willReturn(200L)

            mockMvc.perform(
                post("/api/v1/comments/reply")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").value(200L))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/comments/{commentId}")
    inner class EditComment {

        @Test
        @DisplayName("댓글을 수정하면 서비스가 호출된다")
        fun editsComment() {
            val request = CommentEditRequest("edited")

            mockMvc.perform(
                patch("/api/v1/comments/30")
                    .contentType("application/json")
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)

            verify(commentCommandService).editComment(30L, "edited", member)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/comments/{commentId}")
    inner class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제하면 서비스가 호출된다")
        fun deletesComment() {
            mockMvc.perform(delete("/api/v1/comments/40"))
                .andExpect(status().isOk)

            verify(commentCommandService).deleteComment(40L, member)
        }
    }
}
