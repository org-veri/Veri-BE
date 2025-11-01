package org.veri.be.domain.post.repository.dto;

import org.veri.be.domain.member.entity.Member;

import java.util.Collection;

public record DetailLikeInfoQueryResult(
        Collection<Member> likedMembers,
        Long likeCount,
        Boolean isLiked
) {
}
