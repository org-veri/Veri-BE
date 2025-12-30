package org.veri.be.post.dto.response;

public record LikeInfoResponse(
        Long likeCount,
        boolean isLiked
) {
}
