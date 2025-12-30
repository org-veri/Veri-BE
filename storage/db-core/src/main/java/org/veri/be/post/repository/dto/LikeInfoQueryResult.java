package org.veri.be.post.repository.dto;

public record LikeInfoQueryResult(
        long likeCount,
        boolean isLiked
) {
}
