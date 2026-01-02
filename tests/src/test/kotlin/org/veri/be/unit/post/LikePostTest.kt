package org.veri.be.unit.post

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.veri.be.support.fixture.LikePostFixture
import org.veri.be.support.fixture.MemberFixture
import org.veri.be.support.fixture.PostFixture

class LikePostTest {

    @Nested
    @DisplayName("equals/hashCode")
    inner class EqualsAndHashCode {

        @Test
        @DisplayName("회원과 게시글 ID가 같으면 → 동일하다")
        fun equalsWhenIdsMatch() {
            val member = MemberFixture.aMember().id(1L).build()
            val post = PostFixture.aPost().id(10L).build()
            val first = LikePostFixture.aLikePost().member(member).post(post).build()
            val second = LikePostFixture.aLikePost().member(MemberFixture.aMember().id(1L).build()).post(PostFixture.aPost().id(10L).build()).build()

            assertThat(first).isEqualTo(second).hasSameHashCodeAs(second)
        }

        @Test
        @DisplayName("회원 ID가 다르면 → 동일하지 않다")
        fun notEqualsWhenMemberDiffers() {
            val first = LikePostFixture.aLikePost()
                .member(MemberFixture.aMember().id(1L).build())
                .post(PostFixture.aPost().id(10L).build())
                .build()
            val second = LikePostFixture.aLikePost()
                .member(MemberFixture.aMember().id(2L).build())
                .post(PostFixture.aPost().id(10L).build())
                .build()

            assertThat(first).isNotEqualTo(second)
        }

        @Test
        @DisplayName("게시글 ID가 없으면 → 동일하지 않다")
        fun notEqualsWhenPostIdMissing() {
            val first = LikePostFixture.aLikePost()
                .member(MemberFixture.aMember().id(1L).build())
                .post(PostFixture.aPost().id(10L).build())
                .build()
            val second = LikePostFixture.aLikePost()
                .member(MemberFixture.aMember().id(1L).build())
                .post(PostFixture.aPost().id(null).build())
                .build()

            assertThat(first).isNotEqualTo(second)
        }
    }
}
