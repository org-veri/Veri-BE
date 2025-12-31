package org.veri.be.integration.usecase

import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.veri.be.book.entity.Book
import org.veri.be.book.service.BookRepository
import org.veri.be.comment.dto.request.CommentEditRequest
import org.veri.be.comment.dto.request.CommentPostRequest
import org.veri.be.comment.dto.request.ReplyPostRequest
import org.veri.be.comment.service.CommentCommandService
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.post.dto.request.PostCreateRequest
import org.veri.be.post.service.PostCommandService
import org.veri.be.integration.IntegrationTestSupport

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
        @DisplayName("댓글 작성 성공")
        fun postCommentSuccess() {
            val postId = createPost()
            val request = CommentPostRequest(postId, "Comment content")

            mockMvc.perform(
                post("/api/v1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.result").exists())
        }

        @Test
        @DisplayName("게시글 미존재")
        fun postCommentNotFound() {
            val request = CommentPostRequest(999L, "Content")

            mockMvc.perform(
                post("/api/v1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
        }

        @Test
        @DisplayName("content 누락")
        fun postCommentInvalid() {
            val postId = createPost()
            val request = CommentPostRequest(postId, null)

            mockMvc.perform(
                post("/api/v1/comments")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
        }
    }

    @Nested
    @DisplayName("POST /api/v1/comments/reply")
    inner class PostReply {
        @Test
        @DisplayName("대댓글 성공")
        fun replySuccess() {
            val parentId = createComment()
            val request = ReplyPostRequest(parentId, "Reply content")

            mockMvc.perform(
                post("/api/v1/comments/reply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
        }

        @Test
        @DisplayName("삭제된/존재하지 않는 부모")
        fun replyNotFound() {
            val request = ReplyPostRequest(999L, "Reply")

            mockMvc.perform(
                post("/api/v1/comments/reply")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/comments/{commentId}")
    inner class EditComment {
        @Test
        @DisplayName("본인 댓글 수정")
        fun editSuccess() {
            val commentId = createComment()
            val request = CommentEditRequest("Edited")

            mockMvc.perform(
                patch("/api/v1/comments/$commentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("타인 댓글 수정 시도")
        fun editForbidden() {
            val postId = createPost()
            var otherMember = Member.builder()
                .email("other@prompt.town")
                .nickname("타인")
                .profileImageUrl("https://example.com/other.png")
                .providerId("provider-9999")
                .providerType(ProviderType.KAKAO)
                .build()
            otherMember = memberRepository.save(otherMember)
            val commentId = commentCommandService.postComment(
                CommentPostRequest(postId, "Other comment"),
                otherMember
            )
            val request = CommentEditRequest("Edited")

            mockMvc.perform(
                patch("/api/v1/comments/$commentId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/comments/{commentId}")
    inner class DeleteComment {
        @Test
        @DisplayName("소유 댓글 삭제(soft delete)")
        fun deleteSuccess() {
            val commentId = createComment()

            mockMvc.perform(delete("/api/v1/comments/$commentId"))
                .andExpect(status().isOk)
        }

        @Test
        @DisplayName("이미 삭제된 댓글 재삭제")
        fun alreadyDeleted() {
            val commentId = createComment()

            mockMvc.perform(delete("/api/v1/comments/$commentId"))
                .andExpect(status().isOk)

            mockMvc.perform(delete("/api/v1/comments/$commentId"))
                .andExpect(status().isOk)
        }
    }

    private fun createPost(): Long {
        var book = Book.builder().title("T").image("I").isbn("ISBN").build()
        book = bookRepository.save(book)
        return postCommandService.createPost(
            PostCreateRequest("Post", "Content", listOf(), book.id),
            getMockMember()
        )
    }

    private fun createComment(): Long {
        val postId = createPost()
        return commentCommandService.postComment(
            CommentPostRequest(postId, "Comment"),
            getMockMember()
        )
    }
}
