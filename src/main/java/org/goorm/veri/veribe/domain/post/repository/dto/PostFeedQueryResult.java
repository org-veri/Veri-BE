package org.goorm.veri.veribe.domain.post.repository.dto;

import org.goorm.veri.veribe.domain.book.entity.Book;
import org.goorm.veri.veribe.domain.member.entity.Member;

import java.time.LocalDateTime;

public record PostFeedQueryResult(
        Long postId,
        String title,
        String content,
        String thumbnailImageUrl,
        Member author,
        Book book,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}
