package org.goorm.veri.veribe.domain.member.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.member.dto.MemberResponse;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.domain.member.service.MemberQueryService;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@Tag(name = "멤버 API")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberQueryService memberQueryService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResponse.MemberInfoResponse> myInfo(@AuthenticatedMember Member member) {
        MemberResponse.MemberInfoResponse response = memberQueryService.findMyInfo(member);
        return ApiResponse.ok(response);
    }
}
