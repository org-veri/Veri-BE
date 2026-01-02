package org.veri.be.support.fixture

import org.veri.be.domain.post.entity.LikePost

object LikePostFixture {
    fun aLikePost(): LikePost.LikePostBuilder<*, *> {
        return LikePost.builder()
            .member(MemberFixture.aMember().build())
            .post(PostFixture.aPost().build())
    }
}
