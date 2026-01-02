package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.comment.dto.request.CommentEditRequest
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.dto.request.ReplyPostRequest
import org.veri.be.domain.comment.service.CommentCommandService
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.post.dto.request.PostCreateRequest
import org.veri.be.domain.post.service.PostCommandService
import org.veri.be.integration.IntegrationTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.steps.CommentSteps

class CommentIntegrationTest : IntegrationTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var postCommandService: PostCommandService

    @Autowired
    private lateinit var commentCommandService: CommentCommandService

    @Nested
    @DisplayName("POST /api/v1/comments")
    inner class PostComment {
        @Test
        @DisplayName("댓글을 작성하면 → 201을 반환한다")
        fun postCommentSuccess() {
            val postId = createPost()
            val request = CommentPostRequest(postId, "Comment content")

            CommentSteps.postComment(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").exists())
        }

        @Test
        @DisplayName("게시글이 없으면 → 404를 반환한다")
        fun postCommentNotFound() {
            val request = CommentPostRequest(999L, "Content")

            CommentSteps.postComment(mockMvc, objectMapper, request)
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("content가 누락되면 → 400을 반환한다")
        fun postCommentInvalid() {
            val postId = createPost()
            val request = CommentPostRequest(postId, null)

            CommentSteps.postComment(mockMvc, objectMapper, request)
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments/reply")
    inner class PostReply {
        @Test
        @DisplayName("대댓글을 작성하면 → 201을 반환한다")
        fun replySuccess() {
            val parentId = createComment()
            val request = ReplyPostRequest(parentId, "Reply content")

            CommentSteps.postReply(mockMvc, objectMapper, request)
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("삭제된/존재하지 않는 부모면 → 404를 반환한다")
        fun replyNotFound() {
            val request = ReplyPostRequest(999L, "Reply")

            CommentSteps.postReply(mockMvc, objectMapper, request)
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/comments/{commentId}")
    inner class EditComment {
        @Test
        @DisplayName("본인 댓글을 수정하면 → 200을 반환한다")
        fun editSuccess() {
            val commentId = createComment()
            val request = CommentEditRequest("Edited")

            CommentSteps.editComment(mockMvc, objectMapper, commentId, request)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("타인 댓글을 수정하면 → 403을 반환한다")
        fun editForbidden() {
            val postId = createPost()
            var otherMember = MemberFixture.aMember()
                .email("other@prompt.town")
                .nickname("타인")
                .profileImageUrl("https://example.com/other.png")
                .providerId("provider-9999")
                .providerType(ProviderType.KAKAO)
                .build()
            otherMember = memberRepository.save(otherMember)
            val commentId = commentCommandService.postComment(
                CommentPostRequest(postId, "Other comment"),
                otherMember.id
            )
            val request = CommentEditRequest("Edited")

            CommentSteps.editComment(mockMvc, objectMapper, commentId, request)
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/comments/{commentId}")
    inner class DeleteComment {
        @Test
        @DisplayName("소유 댓글을 삭제하면 → 200을 반환한다")
        fun deleteSuccess() {
            val commentId = createComment()

            CommentSteps.deleteComment(mockMvc, commentId)
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("이미 삭제된 댓글을 재삭제하면 → 200을 반환한다")
        fun alreadyDeleted() {
            val commentId = createComment()

            CommentSteps.deleteComment(mockMvc, commentId)
                .andExpect(status().isOk)

            CommentSteps.deleteComment(mockMvc, commentId)
                .andExpect(status().isOk)
        }
    }

    private fun createPost(): Long {
        var book = BookFixture.aBook().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        return postCommandService.createPost(
            PostCreateRequest("Post", "Content", listOf(), book.id),
            getMockMember().id
        )
    }

    private fun createComment(): Long {
        val postId = createPost()
        return commentCommandService.postComment(
            CommentPostRequest(postId, "Comment"),
            getMockMember().id
        )
    }
}
