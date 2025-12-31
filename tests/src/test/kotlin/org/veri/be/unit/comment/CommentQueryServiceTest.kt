package org.veri.be.unit.comment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.comment.service.CommentQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.post.dto.response.PostDetailResponse
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import java.time.LocalDateTime
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class CommentQueryServiceTest {

    @org.mockito.Mock
    private lateinit var commentRepository: CommentRepository

    private lateinit var commentQueryService: CommentQueryService

    @BeforeEach
    fun setUp() {
        commentQueryService = CommentQueryService(commentRepository)
    }

    @Nested
    @DisplayName("getCommentsByPostId")
    inner class GetCommentsByPostId {

        @Test
        @DisplayName("댓글과 대댓글을 응답으로 매핑한다")
        fun mapsCommentsWithReplies() {
            val author = member(1L, "author@test.com", "author")
            val replier = member(2L, "replier@test.com", "replier")
            val reply = Comment.builder()
                .id(2L)
                .author(replier)
                .content("reply")
                .build()
            val root = Comment.builder()
                .id(1L)
                .author(author)
                .content("root")
                .replies(listOf(reply))
                .build()

            given(commentRepository.findByPostIdWithRepliesAndAuthor(10L)).willReturn(listOf(root))

            val responses = commentQueryService.getCommentsByPostId(10L)

            assertThat(responses).hasSize(1)
            val response = responses[0]
            assertThat(response.commentId()).isEqualTo(1L)
            assertThat(response.content()).isEqualTo("root")
            assertThat(response.author()).isEqualTo(MemberProfileResponse.from(author))
            assertThat(response.replies()).hasSize(1)
            assertThat(response.replies()[0].commentId()).isEqualTo(2L)
        }

        @Test
        @DisplayName("삭제된 댓글은 내용과 작성자를 마스킹한다")
        fun masksDeletedComment() {
            val author = member(1L, "author@test.com", "author")
            val deleted = Comment.builder()
                .id(1L)
                .author(author)
                .content("root")
                .deletedAt(LocalDateTime.now())
                .build()

            given(commentRepository.findByPostIdWithRepliesAndAuthor(10L)).willReturn(listOf(deleted))

            val responses = commentQueryService.getCommentsByPostId(10L)

            assertThat(responses).hasSize(1)
            val response = responses[0]
            assertThat(response.commentId() as Long?).isNull()
            assertThat(response.content()).isEqualTo("삭제된 댓글입니다.")
            assertThat(response.author()).isNull()
            assertThat(response.isDeleted()).isTrue()
        }
    }

    @Nested
    @DisplayName("getCommentById")
    inner class GetCommentById {

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        fun throwsWhenNotFound() {
            given(commentRepository.findByIdWithPostAndAuthor(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { commentQueryService.getCommentById(1L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
            verify(commentRepository).findByIdWithPostAndAuthor(1L)
        }

        @Test
        @DisplayName("댓글을 조회한다")
        fun returnsComment() {
            val comment = Comment.builder().id(1L).content("content").build()
            given(commentRepository.findByIdWithPostAndAuthor(1L)).willReturn(Optional.of(comment))

            val found = commentQueryService.getCommentById(1L)

            assertThat(found.id).isEqualTo(1L)
            verify(commentRepository).findByIdWithPostAndAuthor(1L)
        }
    }

    private fun member(id: Long, email: String, nickname: String): Member {
        return Member.builder()
            .id(id)
            .email(email)
            .nickname(nickname)
            .profileImageUrl("https://example.com/profile.png")
            .providerId("provider-$nickname")
            .providerType(ProviderType.KAKAO)
            .build()
    }
}
