package org.veri.be.domain.common.dto;

import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.repository.dto.MemberProfileQueryResult;

public record MemberProfileResponse(
        Long id,
        String nickname,
        String profileImageUrl
) {

    public static MemberProfileResponse from(Member member) {
        if (member == null) return null;
        return new MemberProfileResponse(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }

    public static MemberProfileResponse from(MemberProfileQueryResult member) {
        if (member == null) return null;
        return new MemberProfileResponse(
                member.id(),
                member.nickname(),
                member.profileImageUrl()
        );
    }
}
