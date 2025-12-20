package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.domain.book.dto.book.BookResponse;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;
import org.veri.be.domain.post.dto.response.PostDetailResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponse;
import org.veri.be.domain.post.dto.response.PostFeedResponseItem;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;

class PostResponseMappingTest {

    @Nested
    @DisplayName("PostFeedResponse")
    class PostFeedResponseMapping {

        @Test
        @DisplayName("페이지 결과를 응답 DTO로 변환한다")
        void mapsPageToResponse() {
            Member author = member(1L, "author@test.com", "author");
            Book book = book();
            PostFeedQueryResult result = new PostFeedQueryResult(
                    1L,
                    "title",
                    "content",
                    "https://example.com/thumb.png",
                    author,
                    book,
                    3L,
                    2L,
                    LocalDateTime.of(2024, 1, 1, 0, 0),
                    true
            );
            PageImpl<PostFeedQueryResult> page = new PageImpl<>(
                    List.of(result),
                    PageRequest.of(1, 10),
                    11
            );

            PostFeedResponse response = new PostFeedResponse(page);

            assertThat(response.page()).isEqualTo(2);
            assertThat(response.size()).isEqualTo(10);
            assertThat(response.totalElements()).isEqualTo(11);
            assertThat(response.totalPages()).isEqualTo(2);
            assertThat(response.posts()).hasSize(1);
            PostFeedResponseItem item = response.posts().get(0);
            assertThat(item.author().id()).isEqualTo(author.getId());
            assertThat(item.author().nickname()).isEqualTo(author.getNickname());
            assertThat(item.book().getIsbn()).isEqualTo(book.getIsbn());
        }
    }

    @Nested
    @DisplayName("PostDetailResponse")
    class PostDetailResponseMapping {

        @Test
        @DisplayName("상세 응답을 조합한다")
        void mapsToDetailResponse() {
            Member author = member(1L, "author@test.com", "author");
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
                    List.of(new MemberProfileQueryResult(2L, "viewer", "https://example.com/profile.png")),
                    1L,
                    true
            );
            List<PostDetailResponse.CommentResponse> comments = List.of();

            PostDetailResponse response = PostDetailResponse.from(post, likeInfo, comments);

            assertThat(response.images()).containsExactly("https://example.com/1.png");
            assertThat(response.commentCount()).isEqualTo(1L);
            assertThat(response.likeCount()).isEqualTo(1L);
            assertThat(response.likedMembers()).hasSize(1);
        }

        @Test
        @DisplayName("삭제된 댓글은 마스킹된다")
        void masksDeletedComment() {
            Member author = member(1L, "author@test.com", "author");
            Comment deleted = Comment.builder()
                    .id(10L)
                    .author(author)
                    .content("content")
                    .deletedAt(LocalDateTime.now())
                    .build();

            PostDetailResponse.CommentResponse response = PostDetailResponse.CommentResponse.fromEntity(deleted);

            assertThat(response.commentId()).isNull();
            assertThat(response.content()).isEqualTo("삭제된 댓글입니다.");
            assertThat(response.author()).isNull();
            assertThat(response.isDeleted()).isTrue();
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
