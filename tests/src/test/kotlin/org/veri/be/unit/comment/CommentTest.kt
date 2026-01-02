package org.veri.be.unit.comment

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CommentFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.post.entity.Post
import java.time.Clock
import java.time.Instant
import java.time.ZoneId

class CommentTest {

    @Nested
    @DisplayName("replyBy")
    inner class ReplyBy {

        @Test
        @DisplayName("대댓글을 생성하면 → 부모/작성자/게시글이 연결된다")
        fun createsReplyWithParentAndAuthor() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val replier = MemberFixture.aMember().id(2L).nickname("replier").build()
            val post = post(author)
            val parent = CommentFixture.aComment()
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
        @DisplayName("작성자가 수정하면 → 내용이 변경된다")
        fun editsContentByAuthor() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val comment = CommentFixture.aComment()
                .author(author)
                .content("before")
                .build()

            comment.editBy(author, "after")

            assertThat(comment.content).isEqualTo("after")
        }

        @Test
        @DisplayName("작성자가 아니면 → ApplicationException이 발생한다")
        fun throwsWhenNotAuthor() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val other = MemberFixture.aMember().id(2L).nickname("other").build()
            val comment = CommentFixture.aComment()
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
        @DisplayName("작성자가 삭제하면 → deletedAt이 설정된다")
        fun marksDeletedAt() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val comment = CommentFixture.aComment()
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
        @DisplayName("작성자가 아니면 → ApplicationException이 발생한다")
        fun throwsWhenNotAuthor() {
            val author = MemberFixture.aMember().id(1L).nickname("author").build()
            val other = MemberFixture.aMember().id(2L).nickname("other").build()
            val comment = CommentFixture.aComment()
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
        @DisplayName("부모가 없으면 → root 댓글이다")
        fun returnsTrueWhenNoParent() {
            val comment = CommentFixture.aComment()
                .author(MemberFixture.aMember().id(1L).nickname("author").build())
                .content("content")
                .build()

            assertThat(comment.isRoot).isTrue()
        }

        @Test
        @DisplayName("부모가 있으면 → root가 아니다")
        fun returnsFalseWhenParentExists() {
            val parent = CommentFixture.aComment()
                .author(MemberFixture.aMember().id(1L).nickname("author").build())
                .content("parent")
                .build()
            val comment = CommentFixture.aComment()
                .author(MemberFixture.aMember().id(2L).nickname("child").build())
                .content("content")
                .parent(parent)
                .build()

            assertThat(comment.isRoot).isFalse()
        }
    }

    private fun post(author: Member): Post {
        val book = BookFixture.aBook()
            .title("book")
            .author("author")
            .isbn("isbn-1")
            .build()
        return PostFixture.aPost()
            .author(author)
            .book(book)
            .title("title")
            .content("content")
            .isPublic(true)
            .build()
    }
}
