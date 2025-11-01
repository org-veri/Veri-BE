package org.goorm.veri.veribe.domain.common.dto;

import org.goorm.veri.veribe.domain.member.entity.Member;

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
}
