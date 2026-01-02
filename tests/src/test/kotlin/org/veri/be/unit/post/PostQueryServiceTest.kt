package org.veri.be.unit.post

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
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.veri.be.api.common.dto.MemberProfileResponse
import org.veri.be.domain.comment.service.CommentQueryService
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult
import org.veri.be.domain.post.controller.enums.PostSortType
import org.veri.be.domain.post.dto.response.PostDetailResponse
import org.veri.be.domain.post.dto.response.PostFeedResponseItem
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult
import org.veri.be.domain.post.service.LikePostQueryService
import org.veri.be.domain.post.service.PostQueryService
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CommentFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture
import java.time.LocalDateTime

@ExtendWith(MockitoExtension::class)
class PostQueryServiceTest {

    @org.mockito.Mock
    private lateinit var postRepository: PostRepository

    @org.mockito.Mock
    private lateinit var likePostQueryService: LikePostQueryService

    @org.mockito.Mock
    private lateinit var commentQueryService: CommentQueryService

    private lateinit var postQueryService: PostQueryService

    @org.mockito.Captor
    private lateinit var pageableCaptor: ArgumentCaptor<Pageable>

    @BeforeEach
    fun setUp() {
        postQueryService = PostQueryService(
            postRepository,
            likePostQueryService,
            commentQueryService
        )
    }

    @Nested
    @DisplayName("getPostFeeds")
    inner class GetPostFeeds {

        @Test
        @DisplayName("정렬 조건에 맞으면 → 페이징 요청을 전달한다")
        fun passesPagingWithSort() {
            given(postRepository.getPostFeeds(any(Pageable::class.java))).willReturn(Page.empty<PostFeedQueryResult>())

            postQueryService.getPostFeeds(1, 20, PostSortType.NEWEST)

            then(postRepository).should().getPostFeeds(pageableCaptor.capture())
            val pageable = pageableCaptor.value
            assertThat(pageable.pageNumber).isEqualTo(1)
            assertThat(pageable.pageSize).isEqualTo(20)
            assertThat(pageable.sort).isEqualTo(PostSortType.NEWEST.sort)
        }
    }

    @Nested
    @DisplayName("getPostsOfMember")
    inner class GetPostsOfMember {

        @Test
        @DisplayName("작성자의 게시글이면 → 응답 DTO로 변환한다")
        fun mapsPostsToResponse() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val book = BookFixture.aBook().id(10L).build()
            val result = PostFeedQueryResult(
                1L,
                "title",
                "content",
                "https://example.com/thumbnail.png",
                author,
                book,
                2L,
                3L,
                LocalDateTime.now(),
                true
            )

            given(postRepository.findAllByAuthorId(1L)).willReturn(listOf(result))

            val responses = postQueryService.getPostsOfMember(1L)

            assertThat(responses).hasSize(1)
            val response: PostFeedResponseItem = responses[0]
            assertThat(response.title()).isEqualTo("title")
            assertThat(response.thumbnail()).isEqualTo("https://example.com/thumbnail.png")
            assertThat(response.author()).isEqualTo(MemberProfileResponse.from(author))
        }
    }

    @Nested
    @DisplayName("getPostById")
    inner class GetPostById {

        @Test
        @DisplayName("존재하지 않으면 → 예외를 던진다")
        fun throwsWhenNotFound() {
            given(postRepository.findById(1L)).willReturn(java.util.Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { postQueryService.getPostById(1L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }
    }

    @Nested
    @DisplayName("getPostDetail")
    inner class GetPostDetail {

        @Test
        @DisplayName("게시글 상세 요청이면 → 정보를 조합한다")
        fun returnsPostDetail() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val requester = MemberFixture.aMember().id(2L).nickname("requester").build()
            val book = BookFixture.aBook().id(10L).build()
            val post = PostFixture.aPost()
                .id(1L)
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .build()
            post.addImage("https://example.com/1.png", 1)
            post.addComment(CommentFixture.aComment().author(author).post(post).content("comment").build())

            val likeInfo = DetailLikeInfoQueryResult(
                listOf(MemberProfileQueryResult(2L, "requester", "https://example.com/profile.png")),
                1L,
                true
            )
            val comments: List<PostDetailResponse.CommentResponse> = listOf()

            given(postRepository.findByIdWithAllAssociations(1L)).willReturn(java.util.Optional.of(post))
            given(likePostQueryService.getDetailLikeInfoOfPost(1L, 2L)).willReturn(likeInfo)
            given(commentQueryService.getCommentsByPostId(1L)).willReturn(comments)

            val response = postQueryService.getPostDetail(1L, requester.id)

            assertThat(response.postId()).isEqualTo(1L)
            assertThat(response.likeCount()).isEqualTo(1L)
            assertThat(response.isLiked()).isTrue()
            assertThat(response.commentCount()).isEqualTo(1L)
            assertThat(response.images()).containsExactly("https://example.com/1.png")
        }

        @Test
        @DisplayName("게시글이 없으면 → 예외를 던진다")
        fun throwsWhenPostMissing() {
            val requester = MemberFixture.aMember().id(2L).nickname("requester").build()
            given(postRepository.findByIdWithAllAssociations(1L)).willReturn(java.util.Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { postQueryService.getPostDetail(1L, requester.id) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }
    }
}
