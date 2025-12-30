package org.veri.be.unit.comment

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.post.entity.Post
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.lib.exception.CommonErrorCode
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CommentTest {

    @Nested
    @DisplayName("replyBy")
    inner class ReplyBy {

        @Test
        @DisplayName("대댓글을 생성하면 부모/작성자/게시글이 연결된다")
        fun createsReplyWithParentAndAuthor() {
            val author = member(1L, "author@test.com", "author")
            val replier = member(2L, "replier@test.com", "replier")
            val post = post(author)
            val parent = Comment.builder()
                .author(author)
                .post(post)
                .content("parent")
                .build()

            val reply = parent.replyBy(replier, "reply")

            assertThat(reply.parent).isEqualTo(parent)
            assertThat(reply.author).isEqualTo(replier)
            assertThat(reply.post).isEqualTo(post)
            assertThat(parent.replies).contains(reply)
        }
    }

    @Nested
    @DisplayName("editBy")
    inner class EditBy {

        @Test
        @DisplayName("작성자가 수정하면 내용이 변경된다")
        fun editsContentByAuthor() {
            val author = member(1L, "author@test.com", "author")
            val comment = Comment.builder()
                .author(author)
                .content("before")
                .build()

            comment.editBy(author, "after")

            assertThat(comment.content).isEqualTo("after")
        }

        @Test
        @DisplayName("작성자가 아니면 ApplicationException이 발생한다")
        fun throwsWhenNotAuthor() {
            val author = member(1L, "author@test.com", "author")
            val other = member(2L, "other@test.com", "other")
            val comment = Comment.builder()
                .author(author)
                .content("content")
                .build()

            assertThatThrownBy { comment.editBy(other, "after") }
                .isInstanceOf(ApplicationException::class.java)
                .hasMessage(CommonErrorCode.DOES_NOT_HAVE_PERMISSION.message)
        }
    }

    @Nested
    @DisplayName("deleteBy")
    inner class DeleteBy {

        @Test
        @DisplayName("작성자가 삭제하면 deletedAt이 설정된다")
        fun marksDeletedAt() {
            val author = member(1L, "author@test.com", "author")
            val comment = Comment.builder()
                .author(author)
                .content("content")
                .build()
            val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

            comment.deleteBy(author, fixedClock)

            assertThat(comment.deletedAt).isEqualTo(
                Instant.parse("2024-01-01T00:00:00Z")
                    .atZone(ZoneId.of("UTC"))
                    .toLocalDateTime()
            )
            assertThat(comment.isDeleted).isTrue()
        }

        @Test
        @DisplayName("작성자가 아니면 ApplicationException이 발생한다")
        fun throwsWhenNotAuthor() {
            val author = member(1L, "author@test.com", "author")
            val other = member(2L, "other@test.com", "other")
            val comment = Comment.builder()
                .author(author)
                .content("content")
                .build()
            val fixedClock = Clock.fixed(Instant.parse("2024-01-01T00:00:00Z"), ZoneId.of("UTC"))

            assertThatThrownBy { comment.deleteBy(other, fixedClock) }
                .isInstanceOf(ApplicationException::class.java)
                .hasMessage(CommonErrorCode.DOES_NOT_HAVE_PERMISSION.message)
        }
    }

    @Nested
    @DisplayName("isRoot")
    inner class IsRoot {

        @Test
        @DisplayName("부모가 없으면 root 댓글이다")
        fun returnsTrueWhenNoParent() {
            val comment = Comment.builder()
                .author(member(1L, "author@test.com", "author"))
                .content("content")
                .build()

            assertThat(comment.isRoot).isTrue()
        }

        @Test
        @DisplayName("부모가 있으면 root가 아니다")
        fun returnsFalseWhenParentExists() {
            val parent = Comment.builder()
                .author(member(1L, "author@test.com", "author"))
                .content("parent")
                .build()
            val comment = Comment.builder()
                .author(member(2L, "child@test.com", "child"))
                .content("content")
                .parent(parent)
                .build()

            assertThat(comment.isRoot).isFalse()
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

    private fun post(author: Member): Post {
        val book = Book.builder()
            .image("https://example.com/book.png")
            .title("book")
            .author("author")
            .isbn("isbn-1")
            .build()
        return Post.builder()
            .author(author)
            .book(book)
            .title("title")
            .content("content")
            .isPublic(true)
            .build()
    }
}
