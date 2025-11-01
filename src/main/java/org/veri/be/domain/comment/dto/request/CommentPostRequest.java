package org.veri.be.domain.comment.dto.request;

public record CommentPostRequest(
        Long postId,
        String content
) {
}
