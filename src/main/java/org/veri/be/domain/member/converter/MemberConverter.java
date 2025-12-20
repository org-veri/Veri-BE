package org.veri.be.domain.member.converter;

import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;

public class MemberConverter {

    private MemberConverter() {
        throw new UnsupportedOperationException("Utility class");
    }

    public static MemberResponse.MemberInfoResponse toMemberInfoResponse(Member member, int numOfBook, int numOfCard) {
        return MemberResponse.MemberInfoResponse.from(member, numOfBook, numOfCard);
    }
}
