package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.service.CommentQueryService;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;
import org.veri.be.domain.post.controller.enums.PostSortType;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponseItem;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
import org.veri.be.domain.post.service.LikePostQueryService;
import org.veri.be.domain.post.service.PostQueryService;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

@ExtendWith(MockitoExtension.class)
class PostQueryServiceTest {

    @Mock
    PostRepository postRepository;

    @Mock
    LikePostQueryService likePostQueryService;

    @Mock
    CommentQueryService commentQueryService;

    PostQueryService postQueryService;

    @Captor
    ArgumentCaptor<Pageable> pageableCaptor;

    @BeforeEach
    void setUp() {
        postQueryService = new PostQueryService(
                postRepository,
                likePostQueryService,
                commentQueryService
        );
    }

    @Nested
    @DisplayName("getPostFeeds")
    class GetPostFeeds {

        @Test
        @DisplayName("정렬 조건에 맞는 페이징 요청을 전달한다")
        void passesPagingWithSort() {
            given(postRepository.getPostFeeds(any(Pageable.class))).willReturn(Page.empty());

            postQueryService.getPostFeeds(1, 20, PostSortType.NEWEST);

            verify(postRepository).getPostFeeds(pageableCaptor.capture());
            Pageable pageable = pageableCaptor.getValue();
            assertThat(pageable.getPageNumber()).isEqualTo(1);
            assertThat(pageable.getPageSize()).isEqualTo(20);
            assertThat(pageable.getSort()).isEqualTo(PostSortType.NEWEST.getSort());
        }

    }

    @Nested
    @DisplayName("getPostsOfMember")
    class GetPostsOfMember {

        @Test
        @DisplayName("작성자의 게시글을 응답 DTO로 변환한다")
        void mapsPostsToResponse() {
            Member author = member(1L, "author@test.com", "author");
            Book book = book();
            PostFeedQueryResult result = new PostFeedQueryResult(
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
            );

            given(postRepository.findAllByAuthorId(1L)).willReturn(List.of(result));

            List<PostFeedResponseItem> responses = postQueryService.getPostsOfMember(1L);

            assertThat(responses).hasSize(1);
            PostFeedResponseItem response = responses.get(0);
            assertThat(response.title()).isEqualTo("title");
            assertThat(response.thumbnail()).isEqualTo("https://example.com/thumbnail.png");
            assertThat(response.author()).isEqualTo(MemberProfileResponse.from(author));
        }
    }

    @Nested
    @DisplayName("getPostById")
    class GetPostById {

        @Test
        @DisplayName("존재하지 않으면 ApplicationException을 던진다")
        void throwsWhenNotFound() {
            given(postRepository.findById(1L)).willReturn(java.util.Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> postQueryService.getPostById(1L),
                    CommonErrorCode.RESOURCE_NOT_FOUND
            );
        }
    }

    @Nested
    @DisplayName("getPostDetail")
    class GetPostDetail {

        @Test
        @DisplayName("게시글 상세 정보를 조합한다")
        void returnsPostDetail() {
            Member author = member(1L, "author@test.com", "author");
            Member requester = member(2L, "request@test.com", "requester");
            Book book = book();
            Post post = Post.builder()
                    .id(1L)
                    .author(author)
                    .book(book)
                    .title("title")
                    .content("content")
                    .build();
            post.addImage("https://example.com/1.png", 1);
            post.addComment(Comment.builder().author(author).post(post).content("comment").build());

            DetailLikeInfoQueryResult likeInfo = new DetailLikeInfoQueryResult(
                    List.of(new MemberProfileQueryResult(2L, "requester", "https://example.com/profile.png")),
                    1L,
                    true
            );
            List<PostDetailResponse.CommentResponse> comments = List.of();

            given(postRepository.findByIdWithAllAssociations(1L)).willReturn(java.util.Optional.of(post));
            given(likePostQueryService.getDetailLikeInfoOfPost(1L, 2L)).willReturn(likeInfo);
            given(commentQueryService.getCommentsByPostId(1L)).willReturn(comments);

            PostDetailResponse response = postQueryService.getPostDetail(1L, requester);

            assertThat(response.postId()).isEqualTo(1L);
            assertThat(response.likeCount()).isEqualTo(1L);
            assertThat(response.isLiked()).isTrue();
            assertThat(response.commentCount()).isEqualTo(1L);
            assertThat(response.images()).containsExactly("https://example.com/1.png");
        }

        @Test
        @DisplayName("게시글이 없으면 ApplicationException을 던진다")
        void throwsWhenPostMissing() {
            Member requester = member(2L, "request@test.com", "requester");
            given(postRepository.findByIdWithAllAssociations(1L)).willReturn(java.util.Optional.empty());

            ExceptionAssertions.assertApplicationException(
                    () -> postQueryService.getPostDetail(1L, requester),
                    CommonErrorCode.RESOURCE_NOT_FOUND
            );
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

    private Book book() {
        return Book.builder()
                .id(10L)
                .title("book")
                .author("author")
                .image("https://example.com/book.png")
                .isbn("isbn-1")
                .build();
    }
}
