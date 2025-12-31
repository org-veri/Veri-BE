package org.veri.be.domain.comment.model

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import java.time.Clock
import java.time.LocalDateTime
import java.time.ZoneId

@DisplayName("Comment Domain Model Tests")
class CommentDomainTest {

    private val fixedClock = Clock.fixed(
        LocalDateTime.of(2025, 12, 31, 12, 0, 0).atZone(ZoneId.systemDefault()).toInstant(),
        ZoneId.systemDefault()
    )

    @Nested
    @DisplayName("CommentId Value Object")
    inner class CommentIdTest {

        @Test
        @DisplayName("should create valid CommentId")
        fun shouldCreateValidCommentId() {
            val commentId = CommentId.of(1L)

            assertThat(commentId.value).isEqualTo(1L)
        }

        @Test
        @DisplayName("should reject zero ID")
        fun shouldRejectZeroId() {
            assertThatThrownBy { CommentId(0L) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Invalid CommentId")
        }

        @Test
        @DisplayName("should reject negative ID")
        fun shouldRejectNegativeId() {
            assertThatThrownBy { CommentId(-1L) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("Invalid CommentId")
        }
    }

    @Nested
    @DisplayName("CommentContent Value Object")
    inner class CommentContentTest {

        @Test
        @DisplayName("should create valid content")
        fun shouldCreateValidContent() {
            val content = CommentContent.of("Hello, World!")

            assertThat(content.value).isEqualTo("Hello, World!")
        }

        @Test
        @DisplayName("should trim whitespace")
        fun shouldTrimWhitespace() {
            val content = CommentContent.of("  Hello  ")

            assertThat(content.value).isEqualTo("Hello")
        }

        @Test
        @DisplayName("should reject blank content")
        fun shouldRejectBlankContent() {
            assertThatThrownBy { CommentContent.of("   ") }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("blank")
        }

        @Test
        @DisplayName("should reject content exceeding max length")
        fun shouldRejectTooLongContent() {
            val longContent = "x".repeat(2001)

            assertThatThrownBy { CommentContent.of(longContent) }
                .isInstanceOf(IllegalArgumentException::class.java)
                .hasMessageContaining("too long")
        }

        @Test
        @DisplayName("should identify deleted content")
        fun shouldIdentifyDeletedContent() {
            val deletedContent = CommentContent.of("삭제된 댓글입니다")

            assertThat(deletedContent.isDeletedContent()).isTrue()
        }
    }

    @Nested
    @DisplayName("Comment Aggregate Root")
    inner class CommentAggregateTest {

        @Test
        @DisplayName("should create root comment")
        fun shouldCreateRootComment() {
            val comment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Test comment"),
                clock = fixedClock
            )

            assertThat(comment.id).isNull()  // v2.1: null before persistence
            assertThat(comment.postId).isEqualTo(1L)
            assertThat(comment.isRoot()).isTrue()
            assertThat(comment.depth).isEqualTo(0)
            assertThat(comment.createdAt).isEqualTo(LocalDateTime.of(2025, 12, 31, 12, 0, 0))
        }

        @Test
        @DisplayName("should create reply to root comment")
        fun shouldCreateReply() {
            val parent = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Parent"),
                clock = fixedClock
            )

            val reply = Comment.createReply(
                parent = parent,
                authorId = 200L,
                content = CommentContent.of("Reply"),
                clock = fixedClock
            )

            assertThat(reply.depth).isEqualTo(1)
            assertThat(reply.parentCommentId).isEqualTo(parent.id)
            assertThat(reply.postId).isEqualTo(parent.postId)
        }

        @Test
        @DisplayName("should reject nested reply (max depth exceeded)")
        fun shouldRejectNestedReply() {
            val parent = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Parent"),
                clock = fixedClock
            )
            val reply = Comment.createReply(
                parent = parent,
                authorId = 200L,
                content = CommentContent.of("Reply"),
                clock = fixedClock
            )

            assertThatThrownBy {
                Comment.createReply(
                    parent = reply,
                    authorId = 300L,
                    content = CommentContent.of("Nested"),
                    clock = fixedClock
                )
            }
                .isInstanceOf(DomainException::class.java)
                .extracting("code")
                .isEqualTo("MAX_DEPTH")
        }

        @Test
        @DisplayName("should reject reply to deleted comment")
        fun shouldRejectReplyToDeletedComment() {
            val deletedParent = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Parent"),
                clock = fixedClock
            ).deleteBy(100L, fixedClock)

            assertThatThrownBy {
                Comment.createReply(
                    parent = deletedParent,
                    authorId = 200L,
                    content = CommentContent.of("Reply"),
                    clock = fixedClock
                )
            }
                .isInstanceOf(DomainException::class.java)
                .extracting("code")
                .isEqualTo("PARENT_DELETED")
        }

        @Test
        @DisplayName("should edit comment by author")
        fun shouldEditCommentByAuthor() {
            val comment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Original"),
                clock = fixedClock
            )

            val edited = comment.editBy(100L, CommentContent.of("Edited"))

            assertThat(edited.content.value).isEqualTo("Edited")
            assertThat(comment.content.value).isEqualTo("Original")  // immutability
        }

        @Test
        @DisplayName("should reject edit by non-author")
        fun shouldRejectEditByNonAuthor() {
            val comment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Original"),
                clock = fixedClock
            )

            assertThatThrownBy {
                comment.editBy(999L, CommentContent.of("Hacked"))
            }
                .isInstanceOf(DomainException::class.java)
                .extracting("code")
                .isEqualTo("UNAUTHORIZED")
        }

        @Test
        @DisplayName("should delete comment by author")
        fun shouldDeleteCommentByAuthor() {
            val comment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("To be deleted"),
                clock = fixedClock
            )

            val deleted = comment.deleteBy(100L, fixedClock)

            assertThat(deleted.deleted).isTrue()
            assertThat(deleted.isDeleted()).isTrue()
            assertThat(deleted.content.value).isEqualTo("삭제된 댓글입니다")
            assertThat(deleted.deletedAt).isNotNull()
            assertThat(comment.deleted).isFalse()  // immutability
            assertThat(comment.isDeleted()).isFalse()
        }

        @Test
        @DisplayName("should reject delete by non-author")
        fun shouldRejectDeleteByNonAuthor() {
            val comment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Content"),
                clock = fixedClock
            )

            assertThatThrownBy { comment.deleteBy(999L, fixedClock) }
                .isInstanceOf(DomainException::class.java)
                .extracting("code")
                .isEqualTo("UNAUTHORIZED")
        }

        @Test
        @DisplayName("should reject edit on deleted comment")
        fun shouldRejectEditOnDeletedComment() {
            val deletedComment = Comment.create(
                postId = 1L,
                postAuthorId = 10L,
                authorId = 100L,
                content = CommentContent.of("Deleted"),
                clock = fixedClock
            ).deleteBy(100L, fixedClock)

            assertThatThrownBy {
                deletedComment.editBy(100L, CommentContent.of("Edit"))
            }
                .isInstanceOf(DomainException::class.java)
                .extracting("code")
                .isEqualTo("ALREADY_DELETED")
        }

        @Test
        @DisplayName("should restore comment from persistence")
        fun shouldRestoreComment() {
            val restored = Comment.restore(
                id = 1L,
                postId = 10L,
                postAuthorId = 100L,
                authorId = 1000L,
                content = "Restored content",
                parentCommentId = null,
                depth = 0,
                isDeleted = false,
                createdAt = LocalDateTime.of(2025, 1, 1, 0, 0, 0),
                deletedAt = null
            )

            assertThat(restored.id).isNotNull()
            assertThat(restored.id!!.value).isEqualTo(1L)
            assertThat(restored.content.value).isEqualTo("Restored content")
        }
    }
}
