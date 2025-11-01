package org.veri.be.domain.post.repository.dto;

import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.member.entity.Member;

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
        LocalDateTime createdAt,
        boolean isPublic
) {
}
