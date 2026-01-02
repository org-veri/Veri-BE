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
import org.veri.be.domain.post.entity.LikePost
import org.veri.be.domain.post.repository.LikePostRepository
import org.veri.be.domain.post.repository.dto.DetailLikeInfoQueryResult
import org.veri.be.domain.post.service.LikePostQueryService
import org.veri.be.support.fixture.LikePostFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture

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
        @DisplayName("좋아요가 있으면 → 수와 멤버 목록을 반환한다")
        fun returnsLikeInfo() {
            val post = PostFixture.aPost().id(1L).title("title").content("content").build()
            val member = MemberFixture.aMember().id(1L).nickname("member").build()
            val like = LikePostFixture.aLikePost().post(post).member(member).build()

            given(fluentQuery.fetch()).willReturn(listOf(like))

            val result: DetailLikeInfoQueryResult = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L)

            assertThat(result.likeCount()).isEqualTo(1L)
            assertThat(result.likedMembers()).hasSize(1)
            assertThat(result.likedMembers()[0].id()).isEqualTo(1L)
            assertThat(result.isLiked()).isTrue()
        }

        @Test
        @DisplayName("요청자와 다른 좋아요가 있으면 → isLiked는 false다")
        fun returnsFalseWhenNotAllMatch() {
            val post = PostFixture.aPost().id(1L).title("title").content("content").build()
            val member = MemberFixture.aMember().id(2L).nickname("other").build()
            val like = LikePostFixture.aLikePost().post(post).member(member).build()

            given(fluentQuery.fetch()).willReturn(listOf(like))

            val result: DetailLikeInfoQueryResult = likePostQueryService.getDetailLikeInfoOfPost(1L, 1L)

            assertThat(result.likeCount()).isEqualTo(1L)
            assertThat(result.isLiked()).isFalse()
        }
    }

    private fun anyProperty(): Property<LikePost, *>? {
        return org.mockito.ArgumentMatchers.any()
    }
}
