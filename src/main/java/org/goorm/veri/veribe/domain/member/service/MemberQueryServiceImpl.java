package org.goorm.veri.veribe.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.exception.MemberErrorCode;
import org.goorm.veri.veribe.domain.member.exception.MemberException;
import org.goorm.veri.veribe.domain.member.repository.MemberRepository;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberQueryServiceImpl implements MemberQueryService {

    private final MemberRepository memberRepository;

    @Override
    public Member findById(Long id) {
        return memberRepository.findById(id).orElseThrow(() ->
                new MemberException(MemberErrorCode.MEMBER_NOT_FOUND));
    }
}
