package org.veri.be.unit.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.data.domain.PageImpl
import org.springframework.data.domain.PageRequest
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.comment.entity.Comment
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult
import org.veri.be.domain.post.dto.response.PostDetailResponse
import org.veri.be.domain.post.dto.response.PostFeedResponse
import org.veri.be.domain.post.dto.response.PostFeedResponseItem
import org.veri.be.domain.post.entity.Post
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult
import java.time.LocalDateTime

class PostResponseMappingTest {

    @Nested
    @DisplayName("PostFeedResponse")
    inner class PostFeedResponseMapping {

        @Test
        @DisplayName("페이지 결과를 응답 DTO로 변환한다")
        fun mapsPageToResponse() {
            val author = member(1L, "author@test.com", "author")
            val book = book()
            val result = PostFeedQueryResult(
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
            )
            val page = PageImpl(
                listOf(result),
                PageRequest.of(1, 10),
                11
            )

            val response = PostFeedResponse(page)

            assertThat(response.page()).isEqualTo(2)
            assertThat(response.size()).isEqualTo(10)
            assertThat(response.totalElements()).isEqualTo(11)
            assertThat(response.totalPages()).isEqualTo(2)
            assertThat(response.posts()).hasSize(1)
            val item: PostFeedResponseItem = response.posts()[0]
            assertThat(item.author().id()).isEqualTo(author.id)
            assertThat(item.author().nickname()).isEqualTo(author.nickname)
            assertThat(item.book().isbn).isEqualTo(book.isbn)
        }
    }

    @Nested
    @DisplayName("PostDetailResponse")
    inner class PostDetailResponseMapping {

        @Test
        @DisplayName("상세 응답을 조합한다")
        fun mapsToDetailResponse() {
            val author = member(1L, "author@test.com", "author")
            val book = book()
            val post = Post.builder()
                .id(1L)
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .build()
            post.addImage("https://example.com/1.png", 1)
            post.addComment(Comment.builder().author(author).post(post).content("comment").build())

            val likeInfo = DetailLikeInfoQueryResult(
                listOf(MemberProfileQueryResult(2L, "viewer", "https://example.com/profile.png")),
                1L,
                true
            )
            val comments: List<PostDetailResponse.CommentResponse> = listOf()

            val response = PostDetailResponse.from(post, likeInfo, comments)

            assertThat(response.images()).containsExactly("https://example.com/1.png")
            assertThat(response.commentCount()).isEqualTo(1L)
            assertThat(response.likeCount()).isEqualTo(1L)
            assertThat(response.likedMembers()).hasSize(1)
        }

        @Test
        @DisplayName("삭제된 댓글은 마스킹된다")
        fun masksDeletedComment() {
            val author = member(1L, "author@test.com", "author")
            val deleted = Comment.builder()
                .id(10L)
                .author(author)
                .content("content")
                .deletedAt(LocalDateTime.now())
                .build()

            val response = PostDetailResponse.CommentResponse.fromEntity(deleted)

            assertThat(response.commentId() as Long?).isNull()
            assertThat(response.content()).isEqualTo("삭제된 댓글입니다.")
            assertThat(response.author()).isNull()
            assertThat(response.isDeleted()).isTrue()
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

    private fun book(): Book {
        return Book.builder()
            .id(10L)
            .title("book")
            .author("author")
            .image("https://example.com/book.png")
            .isbn("isbn-1")
            .build()
    }
}
