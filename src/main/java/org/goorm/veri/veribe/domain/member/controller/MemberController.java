package org.goorm.veri.veribe.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.member.dto.MemberResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.service.MemberQueryService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;

    @GetMapping("/me")
    public DefaultResponse<MemberResponse.MemberInfoResponse> myInfo(@AuthenticatedMember Member member) {
        MemberResponse.MemberInfoResponse response = memberQueryService.findMyInfo(member);
        return DefaultResponse.ok(response);
    }
}
