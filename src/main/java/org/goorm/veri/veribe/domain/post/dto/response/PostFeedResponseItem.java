package org.goorm.veri.veribe.domain.post.dto.response;

import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;

import java.time.LocalDateTime;

public record PostFeedResponseItem(
        Long postId,
        String title,
        String content,
        String thumbnail,
        MemberProfile author,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {

    public static PostFeedResponseItem from(PostFeedQueryResult post) {
        return new PostFeedResponseItem(
                post.postId(),
                post.title(),
                post.content(),
                post.thumbnailImageUrl(),
                MemberProfile.from(post.author()),
                post.likeCount(),
                post.commentCount(),
                post.createdAt()
        );
    }
}
