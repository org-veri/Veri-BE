package org.veri.be.domain.comment.dto.request;

public record ReplyPostRequest(
        Long parentCommentId,
        String content
) {
}
