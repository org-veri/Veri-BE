package org.veri.be.domain.member.converter;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;

@RequiredArgsConstructor(access = lombok.AccessLevel.PRIVATE)
public class MemberConverter {
    public static MemberResponse.MemberInfoResponse toMemberInfoResponse(Member member, int numOfBook, int numOfCard) {
        return MemberResponse.MemberInfoResponse.from(member, numOfBook, numOfCard);
    }
}
