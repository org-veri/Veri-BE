package org.veri.be.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.member.dto.MemberResponse;
import org.veri.be.member.dto.UpdateMemberInfoRequest;
import org.veri.be.member.entity.Member;
import org.veri.be.member.exception.MemberErrorCode;
import org.veri.be.member.repository.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.lib.exception.ApplicationException;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;


    @Transactional
    public MemberResponse.MemberSimpleResponse updateInfo(UpdateMemberInfoRequest request, Member requestMember) {
        if (!requestMember.getNickname().equals(request.nickname()) && memberQueryService.existsByNickname(request.nickname())) {
            throw ApplicationException.of(MemberErrorCode.ALREADY_EXIST_NICKNAME);
        }

        requestMember.updateInfo(request.nickname(), request.profileImageUrl());
        return MemberResponse.MemberSimpleResponse.from(memberRepository.save(requestMember));
    }
}
