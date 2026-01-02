package org.veri.be.unit.comment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.comment.service.CommentQueryService
import org.veri.be.domain.post.dto.response.PostDetailResponse
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.CommentFixture
import org.veri.be.support.fixture.MemberFixture
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
        @DisplayName("댓글과 대댓글이 있으면 → 응답으로 매핑한다")
        fun mapsCommentsWithReplies() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val replier = MemberFixture.aMember().id(2L).nickname("replier").build()
            val reply = CommentFixture.aComment()
                .id(2L)
                .author(replier)
                .content("reply")
                .build()
            val root = CommentFixture.aComment()
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
        @DisplayName("삭제된 댓글이면 → 내용과 작성자를 마스킹한다")
        fun masksDeletedComment() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val deleted = CommentFixture.aComment()
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
        @DisplayName("존재하지 않으면 → 예외를 던진다")
        fun throwsWhenNotFound() {
            given(commentRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { commentQueryService.getCommentById(1L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }

        @Test
        @DisplayName("댓글을 조회하면 → 결과를 반환한다")
        fun returnsComment() {
            val comment = CommentFixture.aComment().id(1L).content("content").build()
            given(commentRepository.findById(1L)).willReturn(Optional.of(comment))

            val found = commentQueryService.getCommentById(1L)

            assertThat(found.id).isEqualTo(1L)
            then(commentRepository).should().findById(1L)
        }
    }
}
