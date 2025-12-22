package org.veri.be.unit.comment;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.lib.exception.ApplicationException;

class CommentTest {

    @Nested
    @DisplayName("replyBy")
    class ReplyBy {

        @Test
        @DisplayName("대댓글을 생성하면 부모/작성자/게시글이 연결된다")
        void createsReplyWithParentAndAuthor() {
            Member author = member(1L, "author@test.com", "author");
            Member replier = member(2L, "replier@test.com", "replier");
            Post post = post(author);
            Comment parent = Comment.builder()
                    .author(author)
                    .post(post)
                    .content("parent")
                    .build();

            Comment reply = parent.replyBy(replier, "reply");

            assertThat(reply.getParent()).isEqualTo(parent);
            assertThat(reply.getAuthor()).isEqualTo(replier);
            assertThat(reply.getPost()).isEqualTo(post);
            assertThat(parent.getReplies()).contains(reply);
        }
    }

    @Nested
    @DisplayName("editBy")
    class EditBy {

        @Test
        @DisplayName("작성자가 수정하면 내용이 변경된다")
        void editsContentByAuthor() {
            Member author = member(1L, "author@test.com", "author");
            Comment comment = Comment.builder()
                    .author(author)
                    .content("before")
                    .build();

            comment.editBy(author, "after");

            assertThat(comment.getContent()).isEqualTo("after");
        }

        @Test
        @DisplayName("작성자가 아니면 ApplicationException이 발생한다")
        void throwsWhenNotAuthor() {
            Member author = member(1L, "author@test.com", "author");
            Member other = member(2L, "other@test.com", "other");
            Comment comment = Comment.builder()
                    .author(author)
                    .content("content")
                    .build();

            assertThatThrownBy(() -> comment.editBy(other, "after"))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CommonErrorCode.DOES_NOT_HAVE_PERMISSION.getMessage());
        }
    }

    @Nested
    @DisplayName("deleteBy")
    class DeleteBy {

        @Test
        @DisplayName("작성자가 삭제하면 deletedAt이 설정된다")
        void marksDeletedAt() {
            Member author = member(1L, "author@test.com", "author");
            Comment comment = Comment.builder()
                    .author(author)
                    .content("content")
                    .build();
            Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));

            comment.deleteBy(author, fixedClock);

            assertThat(comment.getDeletedAt()).isEqualTo(Instant.parse("2024-01-01T00:00:00Z")
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime());
            assertThat(comment.isDeleted()).isTrue();
        }

        @Test
        @DisplayName("작성자가 아니면 ApplicationException이 발생한다")
        void throwsWhenNotAuthor() {
            Member author = member(1L, "author@test.com", "author");
            Member other = member(2L, "other@test.com", "other");
            Comment comment = Comment.builder()
                    .author(author)
                    .content("content")
                    .build();
            Clock fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"));

            assertThatThrownBy(() -> comment.deleteBy(other, fixedClock))
                    .isInstanceOf(ApplicationException.class)
                    .hasMessage(CommonErrorCode.DOES_NOT_HAVE_PERMISSION.getMessage());
        }
    }

    @Nested
    @DisplayName("isRoot")
    class IsRoot {

        @Test
        @DisplayName("부모가 없으면 root 댓글이다")
        void returnsTrueWhenNoParent() {
            Comment comment = Comment.builder()
                    .author(member(1L, "author@test.com", "author"))
                    .content("content")
                    .build();

            assertThat(comment.isRoot()).isTrue();
        }

        @Test
        @DisplayName("부모가 있으면 root가 아니다")
        void returnsFalseWhenParentExists() {
            Comment parent = Comment.builder()
                    .author(member(1L, "author@test.com", "author"))
                    .content("parent")
                    .build();
            Comment comment = Comment.builder()
                    .author(member(2L, "child@test.com", "child"))
                    .content("content")
                    .parent(parent)
                    .build();

            assertThat(comment.isRoot()).isFalse();
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

    private Post post(Member author) {
        Book book = Book.builder()
                .image("https://example.com/book.png")
                .title("book")
                .author("author")
                .isbn("isbn-1")
                .build();
        return Post.builder()
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .isPublic(true)
                .build();
    }
}
