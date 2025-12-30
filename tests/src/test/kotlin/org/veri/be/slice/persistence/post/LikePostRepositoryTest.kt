package org.veri.be.slice.persistence.post

import jakarta.persistence.EntityManager
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.post.entity.LikePost
import org.veri.be.domain.post.entity.Post
import org.veri.be.domain.post.repository.LikePostRepository
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.domain.post.repository.dto.LikeInfoQueryResult
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

class LikePostRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var likePostRepository: LikePostRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("countByPostId")
    inner class CountByPostId {

        @Test
        @DisplayName("게시글의 좋아요 수를 반환한다")
        fun returnsLikeCount() {
            val author = saveMember("author@test.com", "author")
            val liker = saveMember("liker@test.com", "liker")
            val post = savePost(author)
            saveLike(liker, post)

            val count = likePostRepository.countByPostId(post.id)

            assertThat(count).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("existsByPostIdAndMemberId")
    inner class ExistsByPostIdAndMemberId {

        @Test
        @DisplayName("좋아요 여부를 확인한다")
        fun returnsLikeExists() {
            val author = saveMember("author@test.com", "author")
            val liker = saveMember("liker@test.com", "liker")
            val post = savePost(author)
            saveLike(liker, post)

            val exists = likePostRepository.existsByPostIdAndMemberId(post.id, liker.id)

            assertThat(exists).isTrue()
        }
    }

    @Nested
    @DisplayName("deleteByPostIdAndMemberId")
    inner class DeleteByPostIdAndMemberId {

        @Test
        @DisplayName("좋아요를 삭제한다")
        fun deletesLike() {
            val author = saveMember("author@test.com", "author")
            val liker = saveMember("liker@test.com", "liker")
            val post = savePost(author)
            saveLike(liker, post)

            likePostRepository.deleteByPostIdAndMemberId(post.id, liker.id)

            assertThat(likePostRepository.existsByPostIdAndMemberId(post.id, liker.id)).isFalse()
        }
    }

    @Nested
    @DisplayName("getLikeInfoOfPost")
    inner class GetLikeInfoOfPost {

        @Test
        @DisplayName("좋아요 수와 좋아요 여부를 함께 반환한다")
        fun returnsLikeInfo() {
            val author = saveMember("author@test.com", "author")
            val liker = saveMember("liker@test.com", "liker")
            val post = savePost(author)
            saveLike(liker, post)

            val result: LikeInfoQueryResult = likePostRepository.getLikeInfoOfPost(post.id, liker.id)

            assertThat(result.likeCount()).isEqualTo(1)
            assertThat(result.isLiked).isTrue()
        }
    }

    private fun saveMember(email: String, nickname: String): Member {
        return memberRepository.save(
            Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-$nickname")
                .providerType(ProviderType.KAKAO)
                .build()
        )
    }

    private fun savePost(author: Member): Post {
        val book = bookRepository.save(
            Book.builder()
                .image("https://example.com/book.png")
                .title("book")
                .author("author")
                .isbn("isbn-1")
                .build()
        )
        return postRepository.save(
            Post.builder()
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .isPublic(true)
                .build()
        )
    }

    private fun saveLike(member: Member, post: Post) {
        entityManager.persist(
            LikePost.builder()
                .member(member)
                .post(post)
                .build()
        )
    }
}
