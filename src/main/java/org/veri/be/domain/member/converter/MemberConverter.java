package org.veri.be.domain.member.converter;

import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;

public class MemberConverter {

    public static MemberResponse.MemberInfoResponse toMemberInfoResponse(Member member, int numOfBook, int numOfCard) {
        return MemberResponse.MemberInfoResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .image(member.getProfileImageUrl())
                .numOfReadBook(numOfBook)
                .numOfCard(numOfCard)
                .build();
    }
}
