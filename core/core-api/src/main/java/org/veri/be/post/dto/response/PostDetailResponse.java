package org.veri.be.post.dto.response;

import lombok.Builder;
import org.veri.be.book.dto.book.BookResponse;
import org.veri.be.comment.entity.Comment;
import org.veri.be.api.common.dto.MemberProfileResponse;
import org.veri.be.post.entity.Post;
import org.veri.be.post.entity.PostImage;
import org.veri.be.post.repository.dto.DetailLikeInfoQueryResult;

import java.time.LocalDateTime;
import java.util.List;

@Builder
public record PostDetailResponse(
        Long postId,
        String title,
        String content,
        List<String> images,
        MemberProfileResponse author,
        BookResponse book,
        long likeCount,
        boolean isLiked,
        List<MemberProfileResponse> likedMembers,
        List<CommentResponse> comments,
        long commentCount,
        LocalDateTime createdAt
) {
    public static PostDetailResponse from(Post post, DetailLikeInfoQueryResult likeInfo, List<CommentResponse> comments) {
        List<String> imageUrls = post.getImages().stream()
                .map(PostImage::getImageUrl)
                .toList();

        return PostDetailResponse.builder()
                .postId(post.getId())
                .title(post.getTitle())
                .content(post.getContent())
                .images(imageUrls)
                .author(MemberProfileResponse.from(post.getAuthor()))
                .book(BookResponse.from(post.getBook()))
                .likeCount(likeInfo.likeCount())
                .isLiked(likeInfo.isLiked())
                .likedMembers(likeInfo.likedMembers().stream()
                        .map(MemberProfileResponse::from)
                        .toList())
                .comments(comments)
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .build();
    }

    public record CommentResponse(
            Long commentId,
            String content,
            MemberProfileResponse author,
            List<CommentResponse> replies,
            LocalDateTime createdAt,
            boolean isDeleted
    ) {

        public static CommentResponse fromEntity(Comment comment) {
            List<CommentResponse> replies = comment.getReplies().stream()
                    .map(CommentResponse::fromEntity)
                    .toList();

            boolean isDeleted = comment.isDeleted(); // 삭제 댓글은 작성자/내용 마스킹

            return new CommentResponse(
                    isDeleted ? null : comment.getId(),
                    isDeleted ? "삭제된 댓글입니다." : comment.getContent(),
                    isDeleted ? null : MemberProfileResponse.from(comment.getAuthor()),
                    replies,
                    comment.getCreatedAt(),
                    isDeleted
            );
        }
    }
}
