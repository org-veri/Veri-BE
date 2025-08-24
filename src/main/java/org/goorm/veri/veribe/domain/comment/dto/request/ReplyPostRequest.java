package org.goorm.veri.veribe.domain.comment.dto.request;

public record ReplyPostRequest(
        Long parentCommentId,
        String content
) {
}
