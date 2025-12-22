package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.lib.exception.CommonErrorCode;
import org.veri.be.support.assertion.ExceptionAssertions;

class PostTest {

    @Nested
    @DisplayName("addImage")
    class AddImage {

        @Test
        @DisplayName("이미지를 추가하면 순서대로 저장된다")
        void addsImageInOrder() {
            Post post = Post.builder()
                    .author(member(1L, "author@test.com", "author"))
                    .title("title")
                    .content("content")
                    .build();

            post.addImage("https://example.com/1.png", 1);
            post.addImage("https://example.com/2.png", 2);

            assertThat(post.getImages()).hasSize(2);
            assertThat(post.getImages().get(0).getDisplayOrder()).isEqualTo(1L);
            assertThat(post.getImages().get(1).getDisplayOrder()).isEqualTo(2L);
        }
    }

    @Nested
    @DisplayName("authorizeMember")
    class AuthorizeMember {

        @Test
        @DisplayName("작성자가 아니면 예외가 발생한다")
        void throwsWhenNotOwner() {
            Member author = member(1L, "author@test.com", "author");
            Member other = member(2L, "other@test.com", "other");
            Post post = Post.builder()
                    .author(author)
                    .title("title")
                    .content("content")
                    .build();

            ExceptionAssertions.assertApplicationException(
                    () -> post.authorizeMember(other.getId()),
                    CommonErrorCode.DOES_NOT_HAVE_PERMISSION
            );
        }
    }

    @Nested
    @DisplayName("publish/unpublish")
    class PublishUnpublish {

        @Test
        @DisplayName("작성자가 공개 상태로 변경한다")
        void publishes() {
            Member author = member(1L, "author@test.com", "author");
            Post post = Post.builder()
                    .author(author)
                    .isPublic(false)
                    .title("title")
                    .content("content")
                    .build();

            post.publishBy(author);

            assertThat(post.getIsPublic()).isTrue();
        }

        @Test
        @DisplayName("작성자가 비공개 상태로 변경한다")
        void unpublishes() {
            Member author = member(1L, "author@test.com", "author");
            Post post = Post.builder()
                    .author(author)
                    .isPublic(true)
                    .title("title")
                    .content("content")
                    .build();

            post.unpublishBy(author);

            assertThat(post.getIsPublic()).isFalse();
        }
    }

    private Member member(Long id, String email, String nickname) {
        return Member.builder()
                .id(id)
                .email(email)
                .nickname(nickname)
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-" + nickname)
                .providerType(ProviderType.KAKAO)
                .build();
    }
}
