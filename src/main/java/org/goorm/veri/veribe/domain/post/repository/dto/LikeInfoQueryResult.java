package org.goorm.veri.veribe.domain.post.repository.dto;

public record LikeInfoQueryResult(
        long likeCount,
        boolean isLiked
) {
}
