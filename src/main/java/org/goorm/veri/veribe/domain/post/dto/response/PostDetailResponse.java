package org.goorm.veri.veribe.domain.post.dto.response;

import lombok.Builder;
import org.goorm.veri.veribe.domain.comment.entity.Comment;
import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.post.entity.Post;
import org.goorm.veri.veribe.domain.post.entity.PostImage;
import org.goorm.veri.veribe.domain.post.repository.dto.LikeInfoQueryResult;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        List<String> images,
        MemberProfile author,
        long likeCount,
        boolean isLiked,
        List<CommentResponse> comments,
        long commentCount,
        LocalDateTime createdAt

        // Todo. 태그된 책 정보
) {
    public static PostDetailResponse from(Post post, LikeInfoQueryResult likeInfo, List<CommentResponse> comments) {
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList();

        return PostDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .images(imageUrls)
                .author(MemberProfile.from(post.getAuthor()))
                .likeCount(likeInfo.likeCount())
                .isLiked(likeInfo.isLiked())
                .comments(comments)
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
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
