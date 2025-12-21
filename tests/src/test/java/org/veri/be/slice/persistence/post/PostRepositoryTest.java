package org.veri.be.slice.persistence.post;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceUnitUtil;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.comment.entity.Comment;
import org.veri.be.domain.comment.repository.CommentRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.domain.post.repository.dto.PostFeedQueryResult;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class PostRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    CommentRepository commentRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("getPostFeeds")
    class GetPostFeeds {

        @Test
        @DisplayName("공개 게시글만 조회하고 썸네일과 카운트를 함께 반환한다")
        void returnsPublicPostsWithCounts() {
            Member author = saveMember("author@test.com", "author");
            Member liker = saveMember("liker@test.com", "liker");
            Book book = saveBook("isbn-1", "book-1");
            Post publicPost = savePost(author, book, true, "title-1");
            publicPost.addImage("https://example.com/thumbnail.png", 1);
            publicPost.addImage("https://example.com/second.png", 2);
            postRepository.save(publicPost);
            Post privatePost = savePost(author, book, false, "title-2");
            postRepository.save(privatePost);

            saveLike(liker, publicPost);
            saveComment(author, publicPost, "comment");

            Page<PostFeedQueryResult> page = postRepository.getPostFeeds(
                    PageRequest.of(0, 10, Sort.by("id").ascending())
            );

            Map<Long, PostFeedQueryResult> resultMap = page.getContent().stream()
                    .collect(Collectors.toMap(PostFeedQueryResult::postId, Function.identity()));

            assertThat(resultMap).containsKey(publicPost.getId());
            assertThat(resultMap).doesNotContainKey(privatePost.getId());
            assertThat(resultMap.get(publicPost.getId()).thumbnailImageUrl())
                    .isEqualTo("https://example.com/thumbnail.png");
            assertThat(resultMap.get(publicPost.getId()).likeCount()).isEqualTo(1);
            assertThat(resultMap.get(publicPost.getId()).commentCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("findAllByAuthorId")
    class FindAllByAuthorId {

        @Test
        @DisplayName("작성자 기준으로 게시글을 조회한다")
        void returnsPostsByAuthor() {
            Member author = saveMember("author@test.com", "author");
            Member other = saveMember("other@test.com", "other");
            Book book = saveBook("isbn-1", "book-1");
            Post post1 = savePost(author, book, true, "title-1");
            Post post2 = savePost(author, book, false, "title-2");
            postRepository.save(post1);
            postRepository.save(post2);
            postRepository.save(savePost(other, book, true, "title-3"));

            List<PostFeedQueryResult> results = postRepository.findAllByAuthorId(author.getId());

            assertThat(results).hasSize(2);
            assertThat(results).extracting(PostFeedQueryResult::postId)
                    .containsExactlyInAnyOrder(post1.getId(), post2.getId());
        }
    }

    @Nested
    @DisplayName("findByIdWithAllAssociations")
    class FindByIdWithAllAssociations {

        @Test
        @DisplayName("이미지, 작성자, 도서를 fetch join으로 조회한다")
        void fetchesAllAssociations() {
            Member author = saveMember("author@test.com", "author");
            Book book = saveBook("isbn-1", "book-1");
            Post post = savePost(author, book, true, "title-1");
            post.addImage("https://example.com/thumbnail.png", 1);
            postRepository.save(post);

            entityManager.flush();
            entityManager.clear();

            Post found = postRepository.findByIdWithAllAssociations(post.getId()).orElseThrow();
            PersistenceUnitUtil util = entityManager.getEntityManagerFactory().getPersistenceUnitUtil();

            assertThat(util.isLoaded(found.getImages())).isTrue();
            assertThat(util.isLoaded(found.getAuthor())).isTrue();
            assertThat(util.isLoaded(found.getBook())).isTrue();
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

    private Book saveBook(String isbn, String title) {
        return bookRepository.save(Book.builder()
                .image("https://example.com/book.png")
                .title(title)
                .author("author")
                .isbn(isbn)
                .build());
    }

    private Post savePost(Member author, Book book, boolean isPublic, String title) {
        return Post.builder()
                .author(author)
                .book(book)
                .title(title)
                .content("content")
                .isPublic(isPublic)
                .build();
    }

    private void saveLike(Member member, Post post) {
        entityManager.persist(LikePost.builder()
                .member(member)
                .post(post)
                .build());
    }

    private void saveComment(Member author, Post post, String content) {
        commentRepository.save(Comment.builder()
                .author(author)
                .post(post)
                .content(content)
                .build());
    }
}
