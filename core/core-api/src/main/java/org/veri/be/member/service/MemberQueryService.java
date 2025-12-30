package org.veri.be.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.card.repository.CardRepository;
import org.veri.be.member.converter.MemberConverter;
import org.veri.be.member.dto.MemberResponse;
import org.veri.be.member.entity.Member;
import org.veri.be.member.exception.MemberErrorCode;
import org.veri.be.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.lib.exception.ApplicationException;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final ReadingCountProvider readingCountProvider;
    private final CardRepository cardRepository;

    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                ApplicationException.of(MemberErrorCode.NOT_FOUND));
    }

    public MemberResponse.MemberInfoResponse findMyInfo(Member member) {
        int bookCount = Math.toIntExact(readingCountProvider.countReadingsByMemberId(member.getId()));
        int cardCount = cardRepository.countAllByMemberId(member.getId());
        return MemberConverter.toMemberInfoResponse(member, bookCount, cardCount);
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
