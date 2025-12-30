package org.veri.be.post.repository.dto;

import org.veri.be.member.repository.dto.MemberProfileQueryResult;

import java.util.List;

public record DetailLikeInfoQueryResult(
        List<MemberProfileQueryResult> likedMembers,
        Long likeCount,
        Boolean isLiked
) {
}
