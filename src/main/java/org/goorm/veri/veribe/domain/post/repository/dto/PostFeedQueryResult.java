package org.goorm.veri.veribe.domain.post.repository.dto;

import org.goorm.veri.veribe.domain.member.entity.Member;

import java.time.LocalDateTime;

public record PostFeedQueryResult(
        Long postId,
        String title,
        String content,
        Member author,
        long likeCount,
        long commentCount,
        LocalDateTime createdAt
) {
}
