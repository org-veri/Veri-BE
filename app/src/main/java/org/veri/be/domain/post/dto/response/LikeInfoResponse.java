package org.veri.be.domain.post.dto.response;

public record LikeInfoResponse(
        Long likeCount,
        boolean isLiked
) {
}
