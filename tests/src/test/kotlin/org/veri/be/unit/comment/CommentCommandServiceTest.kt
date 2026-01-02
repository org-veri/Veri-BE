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
import org.mockito.BDDMockito.then
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.domain.comment.dto.request.CommentPostRequest
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.comment.service.CommentCommandService
import org.veri.be.domain.comment.service.CommentQueryService
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.post.service.PostQueryService
import org.veri.be.support.assertion.CommentAssert
import org.veri.be.support.fixture.CommentFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture
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
    private lateinit var memberRepository: MemberRepository

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
            memberRepository
        )
    }

    @Nested
    @DisplayName("postComment")
    inner class PostComment {

        @Test
        @DisplayName("댓글을 저장하면 → ID를 반환한다")
        fun savesComment() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            val post = PostFixture.aPost().id(10L).title("title").content("content").build()
            val request = CommentPostRequest(10L, "content")

            given(postQueryService.getPostById(10L)).willReturn(post)
            given(memberRepository.getReferenceById(1L)).willReturn(member)
            given(commentRepository.save(any(Comment::class.java)))
                .willAnswer { invocation ->
                    val saved = invocation.getArgument<Comment>(0)
                    ReflectionTestUtils.setField(saved, "id", 1L)
                    saved
                }

            val result = commentCommandService.postComment(request, member.id)

            then(commentRepository).should().save(commentCaptor.capture())
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
        @DisplayName("대댓글을 저장하면 → ID를 반환한다")
        fun savesReply() {
            val member = MemberFixture.aMember().id(2L).nickname("reply").build()
            val post = PostFixture.aPost().id(10L).title("title").content("content").build()
            val parent = CommentFixture.aComment().id(5L).post(post).author(member).content("parent").build()

            given(commentQueryService.getCommentById(5L)).willReturn(parent)
            given(memberRepository.getReferenceById(2L)).willReturn(member)
            given(commentRepository.save(any(Comment::class.java)))
                .willAnswer { invocation ->
                    val saved = invocation.getArgument<Comment>(0)
                    ReflectionTestUtils.setField(saved, "id", 6L)
                    saved
                }

            val result = commentCommandService.postReply(5L, "reply", member.id)

            then(commentRepository).should().save(commentCaptor.capture())
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
        @DisplayName("댓글 내용을 수정하면 → 저장된다")
        fun editsComment() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            val comment = CommentFixture.aComment().id(1L).author(member).content("before").build()

            given(commentQueryService.getCommentById(1L)).willReturn(comment)
            given(memberRepository.getReferenceById(1L)).willReturn(member)

            commentCommandService.editComment(1L, "after", member.id)

            then(commentRepository).should().save(commentCaptor.capture())
            CommentAssert.assertThat(commentCaptor.value)
                .hasContent("after")
        }
    }

    @Nested
    @DisplayName("deleteComment")
    inner class DeleteComment {

        @Test
        @DisplayName("댓글을 삭제하면 → 삭제 처리된다")
        fun deletesComment() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            val comment = CommentFixture.aComment().id(1L).author(member).content("content").build()

            given(commentQueryService.getCommentById(1L)).willReturn(comment)
            given(memberRepository.getReferenceById(1L)).willReturn(member)

            commentCommandService.deleteComment(1L, member.id)

            then(commentRepository).should().save(commentCaptor.capture())
            CommentAssert.assertThat(commentCaptor.value)
                .isDeleted(true)
        }
    }
}
