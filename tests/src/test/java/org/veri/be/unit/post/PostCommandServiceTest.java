package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Comparator;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.service.BookService;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.dto.request.PostCreateRequest;
import org.veri.be.domain.post.dto.response.LikeInfoResponse;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.domain.post.service.PostCommandService;
import org.veri.be.domain.post.service.PostQueryService;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class PostCommandServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    PostQueryService postQueryService;

    @Mock
    BookService bookService;

    @Mock
    StorageService storageService;

    @Mock
    LikePostRepository likePostRepository;

    PostCommandService postCommandService;

    @Captor
    ArgumentCaptor<Post> postCaptor;

    @Captor
    ArgumentCaptor<LikePost> likePostCaptor;

    @BeforeEach
    void setUp() {
        postCommandService = new PostCommandService(
                postRepository,
                postQueryService,
                bookService,
                storageService,
                likePostRepository
        );
    }

    @Nested
    @DisplayName("createPost")
    class CreatePost {

        @Test
        @DisplayName("이미지와 함께 게시글을 저장한다")
        void savesPostWithImages() {
            Member author = member(1L, "author@test.com", "author");
            Book book = Book.builder()
                    .id(10L)
                    .title("book")
                    .author("author")
                    .image("https://example.com/book.png")
                    .isbn("isbn-1")
                    .build();
            PostCreateRequest request = new PostCreateRequest(
                    "title",
                    "content",
                    List.of("https://example.com/1.png", "https://example.com/2.png"),
                    10L
            );

            given(bookService.getBookById(10L)).willReturn(book);
            given(postRepository.save(any(Post.class))).willAnswer(invocation -> invocation.getArgument(0));

            postCommandService.createPost(request, author);

            verify(postRepository).save(postCaptor.capture());
            Post saved = postCaptor.getValue();

            assertThat(saved.getTitle()).isEqualTo("title");
            assertThat(saved.getContent()).isEqualTo("content");
            assertThat(saved.getAuthor()).isEqualTo(author);
            assertThat(saved.getBook()).isEqualTo(book);
            assertThat(saved.getImages()).hasSize(2);
            List<Long> orders = saved.getImages().stream()
                    .sorted(Comparator.comparingLong(image -> image.getDisplayOrder()))
                    .map(image -> image.getDisplayOrder())
                    .toList();
            assertThat(orders).containsExactly(1L, 2L);
        }
    }

    @Nested
    @DisplayName("deletePost")
    class DeletePost {

        @Test
        @DisplayName("게시글을 삭제한다")
        void deletesPost() {
            Member author = member(1L, "author@test.com", "author");
            Post post = Post.builder()
                    .id(1L)
                    .author(author)
                    .title("title")
                    .content("content")
                    .build();

            given(postQueryService.getPostById(1L)).willReturn(post);

            postCommandService.deletePost(1L, author);

            verify(postRepository).deleteById(1L);
        }
    }

    @Nested
    @DisplayName("publishPost")
    class PublishPost {

        @Test
        @DisplayName("게시글을 공개로 변경한다")
        void publishesPost() {
            Member author = member(1L, "author@test.com", "author");
            Post post = Post.builder()
                    .id(1L)
                    .author(author)
                    .isPublic(false)
                    .title("title")
                    .content("content")
                    .build();

            given(postQueryService.getPostById(1L)).willReturn(post);

            postCommandService.publishPost(1L, author);

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getIsPublic()).isTrue();
        }
    }

    @Nested
    @DisplayName("unPublishPost")
    class UnPublishPost {

        @Test
        @DisplayName("게시글을 비공개로 변경한다")
        void unpublishesPost() {
            Member author = member(1L, "author@test.com", "author");
            Post post = Post.builder()
                    .id(1L)
                    .author(author)
                    .isPublic(true)
                    .title("title")
                    .content("content")
                    .build();

            given(postQueryService.getPostById(1L)).willReturn(post);

            postCommandService.unPublishPost(1L, author);

            verify(postRepository).save(postCaptor.capture());
            assertThat(postCaptor.getValue().getIsPublic()).isFalse();
        }
    }

    @Nested
    @DisplayName("getPresignedUrl")
    class GetPresignedUrl {

        @Test
        @DisplayName("용량이 초과되면 예외가 발생한다")
        void throwsWhenImageTooLarge() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 1024 * 1024L + 1);

            ExceptionAssertions.assertApplicationException(
                    () -> postCommandService.getPresignedUrl(request),
                    CardErrorInfo.IMAGE_TOO_LARGE
            );
        }

        @Test
        @DisplayName("이미지 타입이 아니면 예외가 발생한다")
        void throwsWhenUnsupportedType() {
            PresignedUrlRequest request = new PresignedUrlRequest("application/pdf", 100);

            ExceptionAssertions.assertApplicationException(
                    () -> postCommandService.getPresignedUrl(request),
                    CardErrorInfo.UNSUPPORTED_IMAGE_TYPE
            );
        }

        @Test
        @DisplayName("이미지 업로드용 Presigned URL을 반환한다")
        void returnsPresignedUrl() {
            PresignedUrlRequest request = new PresignedUrlRequest("image/png", 100);
            PresignedUrlResponse response = new PresignedUrlResponse("https://example.com/presigned", "https://example.com/public");

            given(storageService.generatePresignedUrlOfDefault(eq("image/png"), eq(100L))).willReturn(response);

            PresignedUrlResponse result = postCommandService.getPresignedUrl(request);

            assertThat(result).isEqualTo(response);
        }
    }

    @Nested
    @DisplayName("likePost")
    class LikePostAction {

        @Test
        @DisplayName("이미 좋아요가 있으면 저장하지 않는다")
        void returnsLikeInfoWhenAlreadyLiked() {
            Member member = member(1L, "member@test.com", "member");
            given(likePostRepository.existsByPostIdAndMemberId(1L, 1L)).willReturn(true);
            given(likePostRepository.countByPostId(1L)).willReturn(2L);

            LikeInfoResponse result = postCommandService.likePost(1L, member);

            assertThat(result.likeCount()).isEqualTo(2L);
            assertThat(result.isLiked()).isTrue();
            verify(likePostRepository, never()).save(any(LikePost.class));
        }

        @Test
        @DisplayName("좋아요를 저장하고 카운트를 반환한다")
        void savesLikeWhenNotExists() {
            Member member = member(1L, "member@test.com", "member");
            Post post = Post.builder().id(1L).author(member).title("title").content("content").build();

            given(likePostRepository.existsByPostIdAndMemberId(1L, 1L)).willReturn(false);
            given(postQueryService.getPostById(1L)).willReturn(post);
            given(likePostRepository.countByPostId(1L)).willReturn(1L);

            LikeInfoResponse result = postCommandService.likePost(1L, member);

            verify(likePostRepository).save(likePostCaptor.capture());
            assertThat(likePostCaptor.getValue().getPost()).isEqualTo(post);
            assertThat(likePostCaptor.getValue().getMember()).isEqualTo(member);
            assertThat(result.likeCount()).isEqualTo(1L);
            assertThat(result.isLiked()).isTrue();
        }
    }

    @Nested
    @DisplayName("unlikePost")
    class UnlikePostAction {

        @Test
        @DisplayName("좋아요를 삭제하고 카운트를 반환한다")
        void deletesLike() {
            Member member = member(1L, "member@test.com", "member");
            given(likePostRepository.countByPostId(1L)).willReturn(0L);

            LikeInfoResponse result = postCommandService.unlikePost(1L, member);

            verify(likePostRepository).deleteByPostIdAndMemberId(1L, 1L);
            assertThat(result.likeCount()).isEqualTo(0L);
            assertThat(result.isLiked()).isFalse();
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
