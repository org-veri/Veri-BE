package org.veri.be.member.converter;

import lombok.RequiredArgsConstructor;
import org.veri.be.member.dto.MemberResponse;
import org.veri.be.member.entity.Member;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberConverter {
    public static MemberResponse.MemberInfoResponse toMemberInfoResponse(Member member, int numOfBook, int numOfCard) {
        return MemberResponse.MemberInfoResponse.from(member, numOfBook, numOfCard);
    }
}
