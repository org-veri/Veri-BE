package org.veri.be.domain.post.repository.dto;

import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;

import java.util.List;

public record DetailLikeInfoQueryResult(
        List<MemberProfileQueryResult> likedMembers,
        Long likeCount,
        Boolean isLiked
) {
}
