package org.goorm.veri.veribe.domain.member.converter;

import org.goorm.veri.veribe.domain.member.dto.MemberResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;

public class MemberConverter {

    public static MemberResponse.MemberInfoResponse toMemberInfoResponse(Member member, int numOfBook, int numOfCard) {
        return MemberResponse.MemberInfoResponse.builder()
                .email(member.getEmail())
                .nickname(member.getNickname())
                .image(member.getImage())
                .numOfReadBook(numOfBook)
                .numOfCard(numOfCard)
                .build();
    }
}
