package org.goorm.veri.veribe.domain.post.dto.response;

import org.goorm.veri.veribe.domain.comment.entity.Comment;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.post.repository.dto.PostDetailQueryResult;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        MemberProfile author,
        long likeCount,
        boolean isLiked,
        List<CommentResponse> comments,
        long commentCount,
        LocalDateTime createdAt

        // Todo. 태그된 책 정보
) {

    public static PostDetailResponse from(PostDetailQueryResult result, List<CommentResponse> comments) {
        return new PostDetailResponse(
                result.postId(),
                result.title(),
                result.content(),
                MemberProfile.from(result.author()),
                result.likeCount(),
                result.isLiked(),
                comments,
                comments.size(),
                result.createdAt()
        );
    }

    public record CommentResponse(
            Long commentId,
            String content,
            MemberProfile author,
            List<CommentResponse> replies,
            LocalDateTime createdAt
    ) {

        public static CommentResponse fromEntity(Comment comment) {
            List<CommentResponse> replies = comment.getReplies().stream()
                    .map(CommentResponse::fromEntity)
                    .toList();

            return new CommentResponse(
                    comment.getId(),
                    comment.getContent(),
                    MemberProfile.from(comment.getAuthor()),
                    replies,
                    comment.getCreatedAt()
            );
        }
    }
}
