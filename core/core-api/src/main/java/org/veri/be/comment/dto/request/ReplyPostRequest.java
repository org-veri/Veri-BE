package org.veri.be.comment.dto.request;

public record ReplyPostRequest(
        Long parentCommentId,
        String content
) {
}
