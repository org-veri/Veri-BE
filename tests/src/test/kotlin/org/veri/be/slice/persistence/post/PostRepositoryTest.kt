package org.veri.be.slice.persistence.post

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceUnitUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort
import org.veri.be.book.entity.Book
import org.veri.be.book.service.BookRepository
import org.veri.be.comment.entity.Comment
import org.veri.be.comment.service.CommentRepository
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.member.service.MemberRepository
import org.veri.be.post.entity.LikePost
import org.veri.be.post.entity.Post
import org.veri.be.post.service.PostRepository
import org.veri.be.post.repository.dto.PostFeedQueryResult
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

class PostRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("getPostFeeds")
    inner class GetPostFeeds {

        @Test
        @DisplayName("공개 게시글만 조회하고 썸네일과 카운트를 함께 반환한다")
        fun returnsPublicPostsWithCounts() {
            val author = saveMember("author@test.com", "author")
            val liker = saveMember("liker@test.com", "liker")
            val book = saveBook("isbn-1", "book-1")
            val publicPost = savePost(author, book, true, "title-1")
            publicPost.addImage("https://example.com/thumbnail.png", 1)
            publicPost.addImage("https://example.com/second.png", 2)
            postRepository.save(publicPost)
            val privatePost = savePost(author, book, false, "title-2")
            postRepository.save(privatePost)

            saveLike(liker, publicPost)
            saveComment(author, publicPost, "comment")

            val page = postRepository.getPostFeeds(
                PageRequest.of(0, 10, Sort.by("id").ascending())
            )

            val resultMap = page.content.associateBy { it.postId }

            assertThat(resultMap)
                .containsKey(publicPost.id)
                .doesNotContainKey(privatePost.id)
            assertThat(resultMap[publicPost.id]?.thumbnailImageUrl())
                .isEqualTo("https://example.com/thumbnail.png")
            assertThat(resultMap[publicPost.id]?.likeCount()).isEqualTo(1)
            assertThat(resultMap[publicPost.id]?.commentCount()).isEqualTo(1)
        }
    }

    @Nested
    @DisplayName("findAllByAuthorId")
    inner class FindAllByAuthorId {

        @Test
        @DisplayName("작성자 기준으로 게시글을 조회한다")
        fun returnsPostsByAuthor() {
            val author = saveMember("author@test.com", "author")
            val other = saveMember("other@test.com", "other")
            val book = saveBook("isbn-1", "book-1")
            val post1 = savePost(author, book, true, "title-1")
            val post2 = savePost(author, book, false, "title-2")
            postRepository.save(post1)
            postRepository.save(post2)
            postRepository.save(savePost(other, book, true, "title-3"))

            val results = postRepository.findAllByAuthorId(author.id)

            assertThat(results).hasSize(2)
            assertThat(results).extracting<Long> { it.postId() }
                .containsExactlyInAnyOrder(post1.id, post2.id)
        }
    }

    @Nested
    @DisplayName("findByIdWithAllAssociations")
    inner class FindByIdWithAllAssociations {

        @Test
        @DisplayName("이미지, 작성자, 도서를 fetch join으로 조회한다")
        fun fetchesAllAssociations() {
            val author = saveMember("author@test.com", "author")
            val book = saveBook("isbn-1", "book-1")
            val post = savePost(author, book, true, "title-1")
            post.addImage("https://example.com/thumbnail.png", 1)
            postRepository.save(post)

            entityManager.flush()
            entityManager.clear()

            val found = postRepository.findByIdWithAllAssociations(post.id).orElseThrow()
            val util: PersistenceUnitUtil = entityManager.entityManagerFactory.persistenceUnitUtil

            assertThat(util.isLoaded(found.images)).isTrue()
            assertThat(util.isLoaded(found.author)).isTrue()
            assertThat(util.isLoaded(found.book)).isTrue()
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

    private fun saveBook(isbn: String, title: String): Book {
        return bookRepository.save(
            Book.builder()
                .image("https://example.com/book.png")
                .title(title)
                .author("author")
                .isbn(isbn)
                .build()
        )
    }

    private fun savePost(author: Member, book: Book, isPublic: Boolean, title: String): Post {
        return Post.builder()
            .author(author)
            .book(book)
            .title(title)
            .content("content")
            .isPublic(isPublic)
            .build()
    }

    private fun saveLike(member: Member, post: Post) {
        entityManager.persist(
            LikePost.builder()
                .member(member)
                .post(post)
                .build()
        )
    }

    private fun saveComment(author: Member, post: Post, content: String) {
        commentRepository.save(
            Comment.builder()
                .author(author)
                .postId(post.id)
                .content(content)
                .build()
        )
    }
}
