package org.goorm.veri.veribe.domain.common.dto;

import org.goorm.veri.veribe.domain.member.entity.Member;

public record MemberProfile(
        Long id,
        String nickname,
        String profileImageUrl
) {

    public static MemberProfile from(Member member) {
        if (member == null) return null;
        return new MemberProfile(
                member.getId(),
                member.getNickname(),
                member.getProfileImageUrl()
        );
    }
}
