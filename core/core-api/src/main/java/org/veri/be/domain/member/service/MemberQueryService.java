package org.veri.be.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.exception.MemberErrorCode;
import org.veri.be.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.global.auth.context.CurrentMemberInfo;
import org.veri.be.global.auth.context.ThreadLocalCurrentMemberAccessor;
import org.veri.be.lib.exception.ApplicationException;

@Transactional(readOnly = true)
@Service
@RequiredArgsConstructor
public class MemberQueryService {

    private final MemberRepository memberRepository;
    private final ReadingRepository readingRepository;
    private final CardRepository cardRepository;
    private final ThreadLocalCurrentMemberAccessor threadLocalCurrentMemberAccessor;

    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                ApplicationException.of(MemberErrorCode.NOT_FOUND));
    }

    public MemberResponse.MemberInfoResponse findMyInfo(CurrentMemberInfo memberInfo) {
        return MemberResponse.MemberInfoResponse.from(
                threadLocalCurrentMemberAccessor.getMemberOrThrow(),
                readingRepository.countAllByMemberId(memberInfo.id()),
                cardRepository.countAllByMemberId(memberInfo.id())
        );
    }

    public boolean existsByNickname(String nickname) {
        return memberRepository.existsByNickname(nickname);
    }
}
