package org.veri.be.slice.persistence.post;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.PostRepository;
import org.veri.be.domain.post.repository.dto.LikeInfoQueryResult;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class LikePostRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    LikePostRepository likePostRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    BookRepository bookRepository;

    @Autowired
    EntityManager entityManager;

    @Nested
    @DisplayName("countByPostId")
    class CountByPostId {

        @Test
        @DisplayName("게시글의 좋아요 수를 반환한다")
        void returnsLikeCount() {
            Member author = saveMember("author@test.com", "author");
            Member liker = saveMember("liker@test.com", "liker");
            Post post = savePost(author);
            saveLike(liker, post);

            long count = likePostRepository.countByPostId(post.getId());

            assertThat(count).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("existsByPostIdAndMemberId")
    class ExistsByPostIdAndMemberId {

        @Test
        @DisplayName("좋아요 여부를 확인한다")
        void returnsLikeExists() {
            Member author = saveMember("author@test.com", "author");
            Member liker = saveMember("liker@test.com", "liker");
            Post post = savePost(author);
            saveLike(liker, post);

            boolean exists = likePostRepository.existsByPostIdAndMemberId(post.getId(), liker.getId());

            assertThat(exists).isTrue();
        }
    }

    @Nested
    @DisplayName("deleteByPostIdAndMemberId")
    class DeleteByPostIdAndMemberId {

        @Test
        @DisplayName("좋아요를 삭제한다")
        void deletesLike() {
            Member author = saveMember("author@test.com", "author");
            Member liker = saveMember("liker@test.com", "liker");
            Post post = savePost(author);
            saveLike(liker, post);

            likePostRepository.deleteByPostIdAndMemberId(post.getId(), liker.getId());

            assertThat(likePostRepository.existsByPostIdAndMemberId(post.getId(), liker.getId())).isFalse();
        }
    }

    @Nested
    @DisplayName("getLikeInfoOfPost")
    class GetLikeInfoOfPost {

        @Test
        @DisplayName("좋아요 수와 좋아요 여부를 함께 반환한다")
        void returnsLikeInfo() {
            Member author = saveMember("author@test.com", "author");
            Member liker = saveMember("liker@test.com", "liker");
            Post post = savePost(author);
            saveLike(liker, post);

            LikeInfoQueryResult result = likePostRepository.getLikeInfoOfPost(post.getId(), liker.getId());

            assertThat(result.likeCount()).isEqualTo(1);
            assertThat(result.isLiked()).isTrue();
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

    private void saveLike(Member member, Post post) {
        entityManager.persist(LikePost.builder()
                .member(member)
                .post(post)
                .build());
    }
}
