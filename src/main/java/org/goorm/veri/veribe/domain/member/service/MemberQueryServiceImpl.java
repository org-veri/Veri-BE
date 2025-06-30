package org.goorm.veri.veribe.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.repository.MemberBookRepository;
import org.goorm.veri.veribe.domain.card.repository.CardRepository;
import org.goorm.veri.veribe.domain.member.converter.MemberConverter;
import org.goorm.veri.veribe.domain.member.dto.MemberResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.exception.MemberErrorCode;
import org.goorm.veri.veribe.domain.member.exception.MemberException;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;
    private final MemberBookRepository memberBookRepository;
    private final CardRepository cardRepository;

    @Override
    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }

    @Override
    public MemberResponse.MemberInfoResponse findMyInfo(Member member) {
        return MemberConverter.toMemberInfoResponse(member, memberBookRepository.countAllByMember(member), cardRepository.countAllByMemberId(member.getId()));
    }
}
