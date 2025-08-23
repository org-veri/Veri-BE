package org.goorm.veri.veribe.domain.post.dto.response;

import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;

import java.time.LocalDateTime;

public record PostFeedResponse(
        Long postId,
        String title,
        String content,
        MemberProfile author,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {

    public static PostFeedResponse from(PostFeedQueryResult post) {
        return new PostFeedResponse(
                post.postId(),
                post.title(),
                post.content(),
                MemberProfile.from(post.author()),
                post.likeCount(),
                post.commentCount(),
                post.createdAt()
        );
    }
}
