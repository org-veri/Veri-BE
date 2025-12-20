package org.veri.be.slice.persistence.comment;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class CommentRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("findByPostIdAndParentIdIsNull")
    class FindByPostIdAndParentIdIsNull {

        @Test
        @DisplayName("부모가 없는 댓글만 조회한다")
        void returnsRootComments() {
            Member author = saveMember("author@test.com", "author");
            Post post = savePost(author);
            Comment root1 = saveComment(author, post, null, "root-1");
            Comment root2 = saveComment(author, post, null, "root-2");
            saveComment(author, post, root1, "reply");

            List<Comment> results = commentRepository.findByPostIdAndParentIdIsNull(post.getId());

            assertThat(results).hasSize(2);
            assertThat(results).allMatch(comment -> comment.getParent() == null);
            assertThat(results).extracting(Comment::getId)
                    .containsExactlyInAnyOrder(root1.getId(), root2.getId());
        }
    }

    @Nested
    @DisplayName("findByPostIdAndParentIdIsNullOrderByCreatedAtAsc")
    class FindByPostIdAndParentIdIsNullOrderByCreatedAtAsc {

        @Test
        @DisplayName("루트 댓글을 생성 시간 기준으로 정렬한다")
        void returnsRootCommentsInCreatedOrder() {
            Member author = saveMember("author@test.com", "author");
            Post post = savePost(author);
            Comment early = saveComment(author, post, null, "early");
            Comment late = saveComment(author, post, null, "late");

            entityManager.createNativeQuery("UPDATE comment SET created_at = :created WHERE comment_id = :id")
                    .setParameter("created", LocalDateTime.now().minusMinutes(10))
                    .setParameter("id", early.getId())
                    .executeUpdate();
            entityManager.createNativeQuery("UPDATE comment SET created_at = :created WHERE comment_id = :id")
                    .setParameter("created", LocalDateTime.now())
                    .setParameter("id", late.getId())
                    .executeUpdate();
            entityManager.clear();

            List<Comment> results = commentRepository.findByPostIdAndParentIdIsNullOrderByCreatedAtAsc(post.getId());

            assertThat(results).hasSize(2);
            assertThat(results.get(0).getId()).isEqualTo(early.getId());
            assertThat(results.get(1).getId()).isEqualTo(late.getId());
        }
    }

    @Nested
    @DisplayName("findByPostIdWithRepliesAndAuthor")
    class FindByPostIdWithRepliesAndAuthor {

        @Test
        @DisplayName("댓글, 대댓글, 작성자까지 fetch join으로 조회한다")
        void fetchesRepliesAndAuthors() {
            Member author = saveMember("author@test.com", "author");
            Member replier = saveMember("replier@test.com", "replier");
            Post post = savePost(author);
            Comment root = saveComment(author, post, null, "root");
            saveComment(replier, post, root, "reply");

            entityManager.flush();
            entityManager.clear();

            List<Comment> results = commentRepository.findByPostIdWithRepliesAndAuthor(post.getId());
            Comment found = results.get(0);
            PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            assertThat(util.isLoaded(found.getAuthor())).isTrue();
            assertThat(util.isLoaded(found.getReplies())).isTrue();
            assertThat(found.getReplies()).hasSize(1);
            assertThat(util.isLoaded(found.getReplies().get(0).getAuthor())).isTrue();
        }
    }

    private Member saveMember(String email, String nickname) {
        return memberRepository.save(Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build());
    }

    private Post savePost(Member author) {
        Book book = bookRepository.save(Book.builder()
                .image("https://example.com/book.png")
                .title("book")
                .author("author")
                .isbn("isbn-1")
                .build());
        return postRepository.save(Post.builder()
                .author(author)
                .book(book)
                .title("title")
                .content("content")
                .isPublic(true)
                .build());
    }

    private Comment saveComment(Member author, Post post, Comment parent, String content) {
        return commentRepository.save(Comment.builder()
                .author(author)
                .post(post)
                .parent(parent)
                .content(content)
                .build());
    }
}
