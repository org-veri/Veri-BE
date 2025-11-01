package org.goorm.veri.veribe.domain.post.repository.dto;

import org.goorm.veri.veribe.domain.member.entity.Member;

import java.util.List;

public record DetailLikeInfoQueryResult(
        List<Member> likedMembers,
        Long likeCount,
        Boolean isLiked
) {
}
