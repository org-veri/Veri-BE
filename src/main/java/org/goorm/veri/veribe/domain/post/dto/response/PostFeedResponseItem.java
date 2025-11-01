package org.goorm.veri.veribe.domain.post.dto.response;

import org.goorm.veri.veribe.domain.book.dto.book.BookResponse;
import org.goorm.veri.veribe.domain.common.dto.MemberProfileResponse;
import org.goorm.veri.veribe.domain.post.repository.dto.PostFeedQueryResult;

import java.time.LocalDateTime;

public record PostFeedResponseItem(
        Long postId,
        String title,
        String content,
        String thumbnail,
        MemberProfileResponse author,
        BookResponse book,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt,
        boolean isPublic
) {

    public static PostFeedResponseItem from(PostFeedQueryResult post) {
        return new PostFeedResponseItem(
                post.postId(),
                post.title(),
                post.content(),
                post.thumbnailImageUrl(),
                MemberProfileResponse.from(post.author()),
                BookResponse.from(post.book()),
                post.likeCount(),
                post.commentCount(),
                post.createdAt(),
                post.isPublic()
        );
    }
}
