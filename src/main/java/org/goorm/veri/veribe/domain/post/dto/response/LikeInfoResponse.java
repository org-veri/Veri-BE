package org.goorm.veri.veribe.domain.post.dto.response;

public record LikeInfoResponse(
        Long likeCount,
        boolean isLiked
) {
}
