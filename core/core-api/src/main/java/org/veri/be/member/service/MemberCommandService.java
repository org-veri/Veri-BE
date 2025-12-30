package org.veri.be.member.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.member.dto.MemberResponse;
import org.veri.be.member.dto.UpdateMemberInfoRequest;
import org.veri.be.member.entity.Member;
import org.veri.be.member.entity.enums.ProviderType;
import org.veri.be.member.exception.MemberErrorCode;
import org.veri.be.member.service.MemberRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.lib.exception.ApplicationException;
import java.time.Clock;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class MemberCommandService {

    private final MemberQueryService memberQueryService;
    private final MemberRepository memberRepository;
    private final Clock clock;


    @Transactional
    public MemberResponse.MemberSimpleResponse updateInfo(UpdateMemberInfoRequest request, Member requestMember) {
        if (!requestMember.getNickname().equals(request.nickname()) && memberQueryService.existsByNickname(request.nickname())) {
            throw ApplicationException.of(MemberErrorCode.ALREADY_EXIST_NICKNAME);
        }

        requestMember.updateInfo(request.nickname(), request.profileImageUrl());
        return MemberResponse.MemberSimpleResponse.from(memberRepository.save(requestMember));
    }

    @Transactional
    public Member saveOrGetOAuthMember(String email, String nickname, String image, String providerId, ProviderType providerType) {
        Optional<Member> optional = memberQueryService.findByProviderIdAndProviderType(providerId, providerType);
        if (optional.isPresent()) {
            return optional.get();
        }

        Member member = Member.builder()
                .email(email)
                .nickname(nickname)
                .profileImageUrl(image)
                .providerId(providerId)
                .providerType(providerType)
                .build();
        if (memberQueryService.existsByNickname(member.getNickname())) {
            member.updateInfo(
                    member.getNickname() + "_" + clock.millis(),
                    member.getProfileImageUrl());
        }
        return memberRepository.save(member);
    }

    @Transactional
    public Member createMember(Member member) {
        return memberRepository.save(member);
    }
}
