package org.goorm.veri.veribe.domain.post.repository.dto;

import org.goorm.veri.veribe.domain.common.dto.MemberProfile;
import org.goorm.veri.veribe.domain.member.entity.Member;

public record PostDetailQueryResponse(
        Long postId,
        String title,
        String content,
        MemberProfile author,
        long likeCount,
        boolean isLiked
) {
    public PostDetailQueryResponse(Long postId, String title, String content, Member author, long likeCount, boolean isLiked) {
        this(postId, title, content, MemberProfile.from(author), likeCount, isLiked);
    }
}
