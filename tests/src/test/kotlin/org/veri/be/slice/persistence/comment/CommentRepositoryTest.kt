package org.veri.be.slice.persistence.comment

import jakarta.persistence.EntityManager
import jakarta.persistence.PersistenceUnitUtil
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.comment.repository.CommentRepository
import org.veri.be.domain.member.entity.enums.ProviderType
import org.veri.be.domain.member.repository.MemberRepository
import org.veri.be.domain.post.repository.PostRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport
import org.veri.be.support.fixture.BookFixture
import org.veri.be.support.fixture.CommentFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture
import java.time.LocalDateTime

class CommentRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var commentRepository: CommentRepository

    @Autowired
    private lateinit var postRepository: PostRepository

    @Autowired
    private lateinit var memberRepository: MemberRepository

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Autowired
    private lateinit var entityManager: EntityManager

    @Nested
    @DisplayName("findByPostIdAndParentIdIsNull")
    inner class FindByPostIdAndParentIdIsNull {

        @Test
        @DisplayName("부모가 없는 댓글만 조회하면 → 결과를 반환한다")
        fun returnsRootComments() {
            val author = saveMember("author@test.com", "author")
            val post = savePost(author)
            val root1 = saveComment(author, post, null, "root-1")
            val root2 = saveComment(author, post, null, "root-2")
            saveComment(author, post, root1, "reply")

            val results = commentRepository.findByPostIdAndParentIdIsNull(post.id)

            assertThat(results)
                .hasSize(2)
                .allMatch { comment -> comment.parent == null }
            assertThat(results).extracting<Long> { it.id }
                .containsExactlyInAnyOrder(root1.id, root2.id)
        }
    }

    @Nested
    @DisplayName("findByPostIdAndParentIdIsNullOrderByCreatedAtAsc")
    inner class FindByPostIdAndParentIdIsNullOrderByCreatedAtAsc {

        @Test
        @DisplayName("루트 댓글을 생성 시간 기준으로 정렬하면 → 결과를 반환한다")
        fun returnsRootCommentsInCreatedOrder() {
            val author = saveMember("author@test.com", "author")
            val post = savePost(author)
            val early = saveComment(author, post, null, "early")
            val late = saveComment(author, post, null, "late")

            entityManager.createNativeQuery("UPDATE comment SET created_at = :created WHERE comment_id = :id")
                .setParameter("created", LocalDateTime.now().minusMinutes(10))
                .setParameter("id", early.id)
                .executeUpdate()
            entityManager.createNativeQuery("UPDATE comment SET created_at = :created WHERE comment_id = :id")
                .setParameter("created", LocalDateTime.now())
                .setParameter("id", late.id)
                .executeUpdate()
            entityManager.clear()

            val results = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(post.id)

            assertThat(results).hasSize(2)
            assertThat(results[0].id).isEqualTo(early.id)
            assertThat(results[1].id).isEqualTo(late.id)
        }
    }

    @Nested
    @DisplayName("findByPostIdWithRepliesAndAuthor")
    inner class FindByPostIdWithRepliesAndAuthor {

        @Test
        @DisplayName("댓글, 대댓글, 작성자까지 fetch join으로 조회하면 → 로딩된다")
        fun fetchesRepliesAndAuthors() {
            val author = saveMember("author@test.com", "author")
            val replier = saveMember("replier@test.com", "replier")
            val post = savePost(author)
            val root = saveComment(author, post, null, "root")
            saveComment(replier, post, root, "reply")

            entityManager.flush()
            entityManager.clear()

            val results = commentRepository.findByPostIdWithRepliesAndAuthor(post.id)
            val found = results[0]
            val util: PersistenceUnitUtil = entityManager.entityManagerFactory.persistenceUnitUtil

            assertThat(util.isLoaded(found.author)).isTrue()
            assertThat(util.isLoaded(found.replies)).isTrue()
            assertThat(found.replies).hasSize(1)
            assertThat(util.isLoaded(found.replies[0].author)).isTrue()
        }
    }

    private fun saveMember(email: String, nickname: String): org.veri.be.domain.member.entity.Member {
        return memberRepository.save(
            MemberFixture.aMember()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-$nickname")
                .providerType(ProviderType.KAKAO)
                .build()
        )
    }

    private fun savePost(author: org.veri.be.domain.member.entity.Member): org.veri.be.domain.post.entity.Post {
        val book = bookRepository.save(
            BookFixture.aBook()
                .image("https://example.com/book.png")
                .title("book")
                .author("author")
                .isbn("isbn-1")
                .build()
        )
        return postRepository.save(
            PostFixture.aPost()
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .isPublic(true)
                .build()
        )
    }

    private fun saveComment(
        author: org.veri.be.domain.member.entity.Member,
        post: org.veri.be.domain.post.entity.Post,
        parent: org.veri.be.domain.comment.entity.Comment?,
        content: String
    ): org.veri.be.domain.comment.entity.Comment {
        return commentRepository.save(
            CommentFixture.aComment()
                .author(author)
                .post(post)
                .parent(parent)
                .content(content)
                .build()
        )
    }
}
