package org.veri.be.domain.post.repository.dto;

public record LikeInfoQueryResult(
        long likeCount,
        boolean isLiked
) {
}
