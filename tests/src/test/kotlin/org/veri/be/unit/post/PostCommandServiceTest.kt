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
import org.mockito.Mockito.never
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.domain.book.service.BookService
import org.veri.be.domain.card.entity.CardErrorInfo
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.post.dto.request.PostCreateRequest
import org.veri.be.domain.post.dto.response.LikeInfoResponse
import org.veri.be.domain.post.entity.LikePost
import org.veri.be.domain.post.entity.Post
import org.veri.be.domain.post.entity.PostImage
import org.veri.be.domain.post.repository.LikePostRepository
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.domain.post.service.PostCommandService
import org.veri.be.domain.post.service.PostQueryService
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.global.storage.service.StorageService
import org.veri.be.support.assertion.ExceptionAssertions
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture
import java.util.Comparator

@ExtendWith(MockitoExtension::class)
class PostCommandServiceTest {

    @org.mockito.Mock
    private lateinit var postRepository: PostRepository

    @org.mockito.Mock
    private lateinit var postQueryService: PostQueryService

    @org.mockito.Mock
    private lateinit var bookService: BookService

    @org.mockito.Mock
    private lateinit var storageService: StorageService

    @org.mockito.Mock
    private lateinit var likePostRepository: LikePostRepository

    @org.mockito.Mock
    private lateinit var memberRepository: MemberRepository

    private lateinit var postCommandService: PostCommandService

    @org.mockito.Captor
    private lateinit var postCaptor: ArgumentCaptor<Post>

    @org.mockito.Captor
    private lateinit var likePostCaptor: ArgumentCaptor<LikePost>

    @BeforeEach
    fun setUp() {
        postCommandService = PostCommandService(
            postRepository,
            postQueryService,
            bookService,
            storageService,
            likePostRepository,
            memberRepository
        )
    }

    @Nested
    @DisplayName("createPost")
    inner class CreatePost {

        @Test
        @DisplayName("이미지와 함께 게시글을 저장하면 → 저장된다")
        fun savesPostWithImages() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val book = BookFixture.aBook()
                .id(10L)
                .title("book")
                .author("author")
                .isbn("isbn-1")
                .build()
            val request = PostCreateRequest(
                "title",
                "content",
                listOf("https://example.com/1.png", "https://example.com/2.png"),
                10L
            )

            given(bookService.getBookById(10L)).willReturn(book)
            given(memberRepository.getReferenceById(1L)).willReturn(author)
            given(postRepository.save(any(Post::class.java))).willAnswer { invocation -> invocation.getArgument(0) }

            postCommandService.createPost(request, author.id)

            then(postRepository).should().save(postCaptor.capture())
            val saved = postCaptor.value

            assertThat(saved.title).isEqualTo("title")
            assertThat(saved.content).isEqualTo("content")
            assertThat(saved.author).isEqualTo(author)
            assertThat(saved.book).isEqualTo(book)
            assertThat(saved.images).hasSize(2)
            val orders = saved.images
                .sortedWith(Comparator.comparingLong(PostImage::getDisplayOrder))
                .map { it.displayOrder }
            assertThat(orders).containsExactly(1L, 2L)
        }
    }

    @Nested
    @DisplayName("deletePost")
    inner class DeletePost {

        @Test
        @DisplayName("게시글을 삭제하면 → 삭제된다")
        fun deletesPost() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val post = PostFixture.aPost()
                .id(1L)
                .author(author)
                .title("title")
                .content("content")
                .build()

            given(postQueryService.getPostById(1L)).willReturn(post)

            postCommandService.deletePost(1L, author.id)

            then(postRepository).should().deleteById(1L)
        }
    }

    @Nested
    @DisplayName("publishPost")
    inner class PublishPost {

        @Test
        @DisplayName("게시글을 공개로 변경하면 → 저장된다")
        fun publishesPost() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val post = PostFixture.aPost()
                .id(1L)
                .author(author)
                .isPublic(false)
                .title("title")
                .content("content")
                .build()

            given(postQueryService.getPostById(1L)).willReturn(post)
            given(memberRepository.getReferenceById(1L)).willReturn(author)

            postCommandService.publishPost(1L, author.id)

            then(postRepository).should().save(postCaptor.capture())
            assertThat(postCaptor.value.isPublic).isTrue()
        }
    }

    @Nested
    @DisplayName("unPublishPost")
    inner class UnPublishPost {

        @Test
        @DisplayName("게시글을 비공개로 변경하면 → 저장된다")
        fun unpublishesPost() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val post = PostFixture.aPost()
                .id(1L)
                .author(author)
                .isPublic(true)
                .title("title")
                .content("content")
                .build()

            given(postQueryService.getPostById(1L)).willReturn(post)
            given(memberRepository.getReferenceById(1L)).willReturn(author)

            postCommandService.unPublishPost(1L, author.id)

            then(postRepository).should().save(postCaptor.capture())
            assertThat(postCaptor.value.isPublic).isFalse()
        }
    }

    @Nested
    @DisplayName("getPresignedUrl")
    inner class GetPresignedUrl {

        @Test
        @DisplayName("용량이 초과되면 → 예외가 발생한다")
        fun throwsWhenImageTooLarge() {
            val request = PresignedUrlRequest("image/png", 1024 * 1024L + 1)

            ExceptionAssertions.assertApplicationException(
                { postCommandService.getPresignedUrl(request) },
                CardErrorInfo.IMAGE_TOO_LARGE
            )
        }

        @Test
        @DisplayName("이미지 타입이 아니면 → 예외가 발생한다")
        fun throwsWhenUnsupportedType() {
            val request = PresignedUrlRequest("application/pdf", 100)

            ExceptionAssertions.assertApplicationException(
                { postCommandService.getPresignedUrl(request) },
                CardErrorInfo.UNSUPPORTED_IMAGE_TYPE
            )
        }

        @Test
        @DisplayName("요청하면 → 이미지 업로드용 Presigned URL을 반환한다")
        fun returnsPresignedUrl() {
            val request = PresignedUrlRequest("image/png", 100)
            val response = PresignedUrlResponse("https://example.com/presigned", "https://example.com/public")

            given(storageService.generatePresignedUrlOfDefault("image/png", 100L)).willReturn(response)

            val result = postCommandService.getPresignedUrl(request)

            assertThat(result).isEqualTo(response)
        }
    }

    @Nested
    @DisplayName("likePost")
    inner class LikePostAction {

        @Test
        @DisplayName("이미 좋아요가 있으면 → 저장하지 않는다")
        fun returnsLikeInfoWhenAlreadyLiked() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            given(likePostRepository.existsByPostIdAndMemberId(1L, 1L)).willReturn(true)
            given(likePostRepository.countByPostId(1L)).willReturn(2L)

            val result: LikeInfoResponse = postCommandService.likePost(1L, member.id)

            assertThat(result.likeCount()).isEqualTo(2L)
            assertThat(result.isLiked()).isTrue()
            then(likePostRepository).should(never()).save(any(LikePost::class.java))
        }
    }

    @Nested
    @DisplayName("likePost")
    inner class LikePostActionNotExists {

        @Test
        @DisplayName("좋아요를 저장하면 → 카운트를 반환한다")
        fun savesLikeWhenNotExists() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            val post = PostFixture.aPost().id(1L).author(member).title("title").content("content").build()

            given(likePostRepository.existsByPostIdAndMemberId(1L, 1L)).willReturn(false)
            given(postQueryService.getPostById(1L)).willReturn(post)
            given(memberRepository.getReferenceById(1L)).willReturn(member)
            given(likePostRepository.countByPostId(1L)).willReturn(1L)

            val result: LikeInfoResponse = postCommandService.likePost(1L, member.id)

            then(likePostRepository).should().save(likePostCaptor.capture())
            assertThat(likePostCaptor.value.post).isEqualTo(post)
            assertThat(likePostCaptor.value.member).isEqualTo(member)
            assertThat(result.likeCount()).isEqualTo(1L)
            assertThat(result.isLiked()).isTrue()
        }
    }

    @Nested
    @DisplayName("unlikePost")
    inner class UnlikePostAction {

        @Test
        @DisplayName("좋아요를 삭제하면 → 카운트를 반환한다")
        fun deletesLike() {
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            given(likePostRepository.countByPostId(1L)).willReturn(0L)

            val result: LikeInfoResponse = postCommandService.unlikePost(1L, member.id)

            then(likePostRepository).should().deleteByPostIdAndMemberId(1L, 1L)
            assertThat(result.likeCount()).isZero()
            assertThat(result.isLiked()).isFalse()
        }
    }
}
