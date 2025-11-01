package org.veri.be.domain.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.member.dto.MemberResponse;
import org.veri.be.domain.member.dto.UpdateMemberInfoRequest;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.domain.member.exception.MemberErrorInfo;
import org.veri.be.domain.member.repository.MemberRepository;
import org.veri.be.global.exception.http.BadRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;


    @Transactional
    public MemberResponse.MemberSimpleResponse updateInfo(UpdateMemberInfoRequest request, Member requestMember) {
        if (memberQueryService.existsByNickname(request.nickname())) {
            throw new BadRequestException(MemberErrorInfo.ALREADY_EXIST_NICKNAME);
        }

        requestMember.updateInfo(request.nickname(), request.profileImageUrl());
        return MemberResponse.MemberSimpleResponse.from(memberRepository.save(requestMember));
    }
}
