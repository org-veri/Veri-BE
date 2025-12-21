package org.veri.be.domain.comment.dto.request;

import jakarta.validation.constraints.NotNull;

public record CommentPostRequest(
        @NotNull
        Long postId,
        @NotNull
        String content
) {
}
