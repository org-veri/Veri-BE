package org.veri.be.unit.post;

import me.miensoap.fluent.core.FieldStep;
import me.miensoap.fluent.core.FluentQuery;
import me.miensoap.fluent.core.OrderStep;
import me.miensoap.fluent.core.Property;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.entity.enums.ProviderType;
import org.veri.be.domain.post.entity.LikePost;
import org.veri.be.domain.post.entity.Post;
import org.veri.be.domain.post.repository.LikePostRepository;
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult;
import org.veri.be.domain.post.service.LikePostQueryService;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
class LikePostQueryServiceTest {

    @Mock
    LikePostRepository likePostRepository;

    @Mock
    FluentQuery<LikePost> fluentQuery;

    @Mock
    FieldStep<LikePost> fieldStep;

    @Mock
    OrderStep<LikePost> orderStep;

    LikePostQueryService likePostQueryService;

    @BeforeEach
    void setUp() {
        likePostQueryService = new LikePostQueryService(likePostRepository);
        given(likePostRepository.query()).willReturn(fluentQuery);
        given(fluentQuery.fetchJoin(anyProperty())).willReturn(fluentQuery);
        given(fluentQuery.where(anyProperty())).willReturn(fieldStep);
        given(fieldStep.equalTo(1L)).willReturn(fluentQuery);
        given(fluentQuery.distinct()).willReturn(fluentQuery);
        given(fluentQuery.orderBy(anyProperty())).willReturn(orderStep);
        given(orderStep.ascending()).willReturn(fluentQuery);
    }

    @Nested
    @DisplayName("getDetailLikeInfoOfPost")
    class GetDetailLikeInfoOfPost {

        @Test
        @DisplayName("좋아요 수와 멤버 목록을 반환한다")
        void returnsLikeInfo() {
            Post post = Post.builder().id(1L).title("title").content("content").build();
            Member member = member(1L, "member@test.com", "member");
            LikePost like = LikePost.builder().post(post).member(member).build();

            given(fluentQuery.fetch()).willReturn(List.of(like));

            DetailLikeInfoQueryResult result = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L);

            assertThat(result.likeCount()).isEqualTo(1L);
            assertThat(result.likedMembers()).hasSize(1);
            assertThat(result.likedMembers().getFirst().id()).isEqualTo(1L);
            assertThat(result.isLiked()).isTrue();
        }

        @Test
        @DisplayName("요청자와 다른 좋아요가 있으면 isLiked는 false다")
        void returnsFalseWhenNotAllMatch() {
            Post post = Post.builder().id(1L).title("title").content("content").build();
            Member member = member(2L, "other@test.com", "other");
            LikePost like = LikePost.builder().post(post).member(member).build();

            given(fluentQuery.fetch()).willReturn(List.of(like));

            DetailLikeInfoQueryResult result = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L);

            assertThat(result.likeCount()).isEqualTo(1L);
            assertThat(result.isLiked()).isFalse();
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

    private Property<LikePost, ?> anyProperty() {
        return org.mockito.ArgumentMatchers.any();
    }
}
