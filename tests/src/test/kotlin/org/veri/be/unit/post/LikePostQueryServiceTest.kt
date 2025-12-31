package org.veri.be.unit.post

import me.miensoap.fluent.core.FieldStep
import me.miensoap.fluent.core.FluentQuery
import me.miensoap.fluent.core.OrderStep
import me.miensoap.fluent.core.Property
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.BDDMockito.given
import org.mockito.junit.jupiter.MockitoExtension
import org.veri.be.member.entity.Member
import org.veri.be.member.entity.enums.ProviderType
import org.veri.be.post.entity.LikePost
import org.veri.be.post.entity.Post
import org.veri.be.post.service.LikePostRepository
import org.veri.be.post.repository.dto.DetailLikeInfoQueryResult
import org.veri.be.post.service.LikePostQueryService

@ExtendWith(MockitoExtension::class)
class LikePostQueryServiceTest {

    @org.mockito.Mock
    private lateinit var likePostRepository: LikePostRepository

    @org.mockito.Mock
    private lateinit var fluentQuery: FluentQuery<LikePost>

    @org.mockito.Mock
    private lateinit var fieldStep: FieldStep<LikePost>

    @org.mockito.Mock
    private lateinit var orderStep: OrderStep<LikePost>

    private lateinit var likePostQueryService: LikePostQueryService

    @BeforeEach
    fun setUp() {
        likePostQueryService = LikePostQueryService(likePostRepository)
        given(likePostRepository.query()).willReturn(fluentQuery)
        given(fluentQuery.fetchJoin(anyProperty())).willReturn(fluentQuery)
        given(fluentQuery.where(anyProperty())).willReturn(fieldStep)
        given(fieldStep.equalTo(1L)).willReturn(fluentQuery)
        given(fluentQuery.distinct()).willReturn(fluentQuery)
        given(fluentQuery.orderBy(anyProperty())).willReturn(orderStep)
        given(orderStep.ascending()).willReturn(fluentQuery)
    }

    @Nested
    @DisplayName("getDetailLikeInfoOfPost")
    inner class GetDetailLikeInfoOfPost {

        @Test
        @DisplayName("좋아요 수와 멤버 목록을 반환한다")
        fun returnsLikeInfo() {
            val post = Post.builder().id(1L).title("title").content("content").build()
            val member = member(1L, "member@test.com", "member")
            val like = LikePost.builder().post(post).member(member).build()

            given(fluentQuery.fetch()).willReturn(listOf(like))

            val result: DetailLikeInfoQueryResult = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L)

            assertThat(result.likeCount()).isEqualTo(1L)
            assertThat(result.likedMembers()).hasSize(1)
            assertThat(result.likedMembers()[0].id()).isEqualTo(1L)
            assertThat(result.isLiked()).isTrue()
        }

        @Test
        @DisplayName("요청자와 다른 좋아요가 있으면 isLiked는 false다")
        fun returnsFalseWhenNotAllMatch() {
            val post = Post.builder().id(1L).title("title").content("content").build()
            val member = member(2L, "other@test.com", "other")
            val like = LikePost.builder().post(post).member(member).build()

            given(fluentQuery.fetch()).willReturn(listOf(like))

            val result: DetailLikeInfoQueryResult = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L)

            assertThat(result.likeCount()).isEqualTo(1L)
            assertThat(result.isLiked()).isFalse()
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

    private fun anyProperty(): Property<LikePost, *>? {
        return org.mockito.ArgumentMatchers.any()
    }
}
