package org.veri.be.slice.web

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
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
import org.veri.be.global.auth.JwtClaimsPayload
import org.veri.be.global.auth.context.AuthenticatedMemberResolver
import org.veri.be.global.auth.context.CurrentMemberAccessor
import org.veri.be.global.auth.context.CurrentMemberInfo
import org.veri.be.lib.response.ApiResponseAdvice
import org.veri.be.support.ControllerTestSupport
import org.veri.be.support.fixture.MemberFixture
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CommentControllerTest : ControllerTestSupport() {

    @org.mockito.Mock
    private lateinit var commentCommandService: CommentCommandService

    private lateinit var member: Member
    private lateinit var memberInfo: CurrentMemberInfo

    @BeforeEach
    fun setUp() {
        member = MemberFixture.aMember()
            .id(1L)
            .providerType(ProviderType.KAKAO)
            .build()

        memberInfo = CurrentMemberInfo.from(JwtClaimsPayload(member.id, member.email, member.nickname, false))
        val controller = CommentController(commentCommandService)
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
            .setControllerAdvice(ApiResponseAdvice())
            .setCustomArgumentResolvers(
                AuthenticatedMemberResolver(testMemberAccessor(memberInfo))
            )
            .build()
    }

    private fun testMemberAccessor(memberInfo: CurrentMemberInfo): CurrentMemberAccessor {
        return object : CurrentMemberAccessor {
            override fun getCurrentMemberInfoOrNull() = memberInfo
            override fun getCurrentMember() = Optional.empty<Member>()
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments")
    inner class PostComment {

        @Test
        @DisplayName("댓글을 작성하면 → ID를 반환한다")
        fun returnsCommentId() {
            val request = CommentPostRequest(10L, "content")
            given(commentCommandService.postComment(request, member.id)).willReturn(100L)

            postJson("/api/v1/comments", request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").value(100L))
        }

        @Test
        @DisplayName("필수 필드가 누락되면 → 400을 반환한다")
        fun returns400WhenFieldMissing() {
            val request = CommentPostRequest(null, null)

            postJson("/api/v1/comments", request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments/reply")
    inner class PostReply {

        @Test
        @DisplayName("대댓글을 작성하면 → ID를 반환한다")
        fun returnsReplyId() {
            val request = ReplyPostRequest(20L, "reply")
            given(commentCommandService.postReply(20L, "reply", member.id)).willReturn(200L)

            postJson("/api/v1/comments/reply", request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").value(200L))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/comments/{commentId}")
    inner class EditComment {

        @Test
        @DisplayName("댓글을 수정하면 → 서비스가 호출된다")
        fun editsComment() {
            val request = CommentEditRequest("edited")

            patchJson("/api/v1/comments/30", request)
                .andExpect(status().isOk)

            then(commentCommandService).should().editComment(30L, "edited", member.id)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/comments/{commentId}")
    inner class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제하면 → 서비스가 호출된다")
        fun deletesComment() {
            delete("/api/v1/comments/40")
                .andExpect(status().isOk)

            then(commentCommandService).should().deleteComment(40L, member.id)
        }
    }
}
