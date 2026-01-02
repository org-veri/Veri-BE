package org.veri.be.support.fixture

import org.veri.be.domain.comment.entity.Comment

object CommentFixture {
    fun aComment(): Comment.CommentBuilder<*, *> {
        return Comment.builder()
            .content("comment")
            .post(PostFixture.aPost().build())
            .author(MemberFixture.aMember().build())
    }
}
