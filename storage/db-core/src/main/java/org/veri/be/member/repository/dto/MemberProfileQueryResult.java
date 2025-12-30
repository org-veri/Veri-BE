package org.veri.be.member.repository.dto;

import org.veri.be.member.entity.Member;

public record MemberProfileQueryResult(
        Long id,
        String nickname,
        String profileImageUrl
) {

    public static MemberProfileQueryResult from(Member member) {
        if (member == null) return null;
        return new MemberProfileQueryResult(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }
}
