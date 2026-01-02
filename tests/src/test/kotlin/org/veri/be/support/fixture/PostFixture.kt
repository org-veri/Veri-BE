package org.veri.be.support.fixture

import org.veri.be.domain.post.entity.Post

object PostFixture {
    fun aPost(): Post.PostBuilder<*, *> {
        return Post.builder()
            .author(MemberFixture.aMember().build())
            .title("title")
            .content("content")
            .book(BookFixture.aBook().build())
            .isPublic(true)
    }
}
