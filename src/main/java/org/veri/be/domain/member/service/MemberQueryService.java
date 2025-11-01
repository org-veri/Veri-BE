package org.veri.be.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.converter.MemberConverter;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.exception.MemberErrorInfo;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.exception.http.NotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final ReadingRepository memberBookRepository;
    private final CardRepository cardRepository;

    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new NotFoundException(MemberErrorInfo.NOT_FOUND));
    }

    public MemberResponse.MemberInfoResponse findMyInfo(Member member) {
        return MemberConverter.toMemberInfoResponse(member, memberBookRepository.countAllByMember(member), cardRepository.countAllByMemberId(member.getId()));
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
