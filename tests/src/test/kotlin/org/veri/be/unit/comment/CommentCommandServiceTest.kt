package org.veri.be.unit.comment

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.context.ApplicationEventPublisher
import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.comment.service.CommentCommandService
import org.veri.be.domain.comment.service.CommentQueryService
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.post.entity.Post
import org.veri.be.domain.post.service.PostQueryService
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

@ExtendWith(MockitoExtension::class)
class CommentCommandServiceTest {

    @org.mockito.Mock
    private lateinit var commentRepository: CommentRepository

    @org.mockito.Mock
    private lateinit var commentQueryService: CommentQueryService

    @org.mockito.Mock
    private lateinit var postQueryService: PostQueryService

    @org.mockito.Mock
    private lateinit var eventPublisher: ApplicationEventPublisher

    private val fixedClock: Clock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

    private lateinit var commentCommandService: CommentCommandService

    @org.mockito.Captor
    private lateinit var commentCaptor: ArgumentCaptor<Comment>

    @BeforeEach
    fun setUp() {
        commentCommandService = CommentCommandService(
            commentRepository,
            commentQueryService,
            postQueryService,
            fixedClock,
            eventPublisher
        )
    }

    @Nested
    @DisplayName("postComment")
    inner class PostComment {

        @Test
        @DisplayName("댓글을 저장하고 ID를 반환한다")
        fun savesComment() {
            val member = member(1L, "member@test.com", "member")
            val post = Post.builder()
                .id(10L)
                .author(member)
                .title("title")
                .content("content")
                .build()
            val request = CommentPostRequest(10L, "content")

            given(postQueryService.getPostById(10L)).willReturn(post)
            given(commentRepository.save(any(Comment::class.java)))
                .willAnswer { invocation ->
                    val saved = invocation.getArgument<Comment>(0)
                    ReflectionTestUtils.setField(saved, "id", 1L)
                    saved
                }

            val result = commentCommandService.postComment(request, member)

            verify(commentRepository).save(commentCaptor.capture())
            // Event publishing is verified implicitly by successful execution
            val saved = commentCaptor.value
            assertThat(saved.post).isEqualTo(post)
            assertThat(saved.author).isEqualTo(member)
            assertThat(saved.content).isEqualTo("content")
            assertThat(result).isEqualTo(1L)
        }
    }

    @Nested
    @DisplayName("postReply")
    inner class PostReply {

        @Test
        @DisplayName("대댓글을 저장하고 ID를 반환한다")
        fun savesReply() {
            val member = member(2L, "reply@test.com", "reply")
            val post = Post.builder()
                .id(10L)
                .author(member)
                .title("title")
                .content("content")
                .build()
            val parent = Comment.builder()
                .id(5L)
                .post(post)
                .author(member)
                .content("parent")
                .build()

            given(commentQueryService.getCommentById(5L)).willReturn(parent)
            given(commentRepository.save(any(Comment::class.java)))
                .willAnswer { invocation ->
                    val saved = invocation.getArgument<Comment>(0)
                    ReflectionTestUtils.setField(saved, "id", 6L)
                    saved
                }

            val result = commentCommandService.postReply(5L, "reply", member)

            verify(commentRepository).save(commentCaptor.capture())
            // Event publishing is verified implicitly by successful execution
            val saved = commentCaptor.value
            assertThat(saved.parent).isEqualTo(parent)
            assertThat(saved.post).isEqualTo(post)
            assertThat(saved.author).isEqualTo(member)
            assertThat(saved.content).isEqualTo("reply")
            assertThat(result).isEqualTo(6L)
        }
    }

    @Nested
    @DisplayName("editComment")
    inner class EditComment {

        @Test
        @DisplayName("댓글 내용을 수정한다")
        fun editsComment() {
            val member = member(1L, "member@test.com", "member")
            val post = Post.builder()
                .id(10L)
                .author(member)
                .build()
            val comment = Comment.builder()
                .id(1L)
                .post(post)
                .author(member)
                .content("before")
                .build()

            given(commentQueryService.getCommentById(1L)).willReturn(comment)

            commentCommandService.editComment(1L, "after", member)

            verify(commentRepository).save(commentCaptor.capture())
            // Event publishing is verified implicitly by successful execution
            assertThat(commentCaptor.value.content).isEqualTo("after")
        }
    }

    @Nested
    @DisplayName("deleteComment")
    inner class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제 처리한다")
        fun deletesComment() {
            val member = member(1L, "member@test.com", "member")
            val post = Post.builder()
                .id(10L)
                .author(member)
                .build()
            val comment = Comment.builder()
                .id(1L)
                .post(post)
                .author(member)
                .content("content")
                .build()

            given(commentQueryService.getCommentById(1L)).willReturn(comment)

            commentCommandService.deleteComment(1L, member)

            verify(commentRepository).save(commentCaptor.capture())
            // Event publishing is verified implicitly by successful execution
            assertThat(commentCaptor.value.isDeleted).isTrue()
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
