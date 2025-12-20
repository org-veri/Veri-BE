package org.veri.be.unit.post;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;

class LikePostTest {

    @Nested
    @DisplayName("equals/hashCode")
    class EqualsAndHashCode {

        @Test
        @DisplayName("회원과 게시글 ID가 같으면 동일하다")
        void equalsWhenIdsMatch() {
            Member member = member(1L);
            Post post = post(10L);
            LikePost first = LikePost.builder().member(member).post(post).build();
            LikePost second = LikePost.builder().member(member(1L)).post(post(10L)).build();

            assertThat(first).isEqualTo(second);
            assertThat(first.hashCode()).isEqualTo(second.hashCode());
        }

        @Test
        @DisplayName("회원 ID가 다르면 동일하지 않다")
        void notEqualsWhenMemberDiffers() {
            LikePost first = LikePost.builder().member(member(1L)).post(post(10L)).build();
            LikePost second = LikePost.builder().member(member(2L)).post(post(10L)).build();

            assertThat(first).isNotEqualTo(second);
        }

        @Test
        @DisplayName("게시글 ID가 없으면 동일하지 않다")
        void notEqualsWhenPostIdMissing() {
            LikePost first = LikePost.builder().member(member(1L)).post(post(10L)).build();
            LikePost second = LikePost.builder().member(member(1L)).post(post(null)).build();

            assertThat(first).isNotEqualTo(second);
        }
    }

    private Member member(Long id) {
        return Member.builder()
                .id(id)
                .email("member@test.com")
                .nickname("member")
                .profileImageUrl("https://example.com/profile.png")
                .providerId("provider-1")
                .providerType(ProviderType.KAKAO)
                .build();
    }

    private Post post(Long id) {
        return Post.builder()
                .id(id)
                .title("title")
                .content("content")
                .author(member(1L))
                .build();
    }
}
