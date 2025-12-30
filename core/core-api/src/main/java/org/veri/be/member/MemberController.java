package org.veri.be.member;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.veri.be.global.auth.context.AuthenticatedMember;
import org.veri.be.member.dto.MemberResponse;
import org.veri.be.member.dto.UpdateMemberInfoRequest;
import org.veri.be.member.entity.Member;
import org.veri.be.member.service.MemberCommandService;
import org.veri.be.member.service.MemberQueryService;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.web.bind.annotation.*;

@Tag(name = "사용자")
@RestController
@RequestMapping("/api/v1/members")
@RequiredArgsConstructor
public class MemberController {

    private final MemberCommandService memberCommandService;
    private final MemberQueryService memberQueryService;

    @Operation(summary = "내 정보 조회", description = "로그인한 사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ApiResponse<MemberResponse.MemberInfoResponse> myInfo(@AuthenticatedMember Member member) {
        MemberResponse.MemberInfoResponse response = memberQueryService.findMyInfo(member);
        return ApiResponse.ok(response);
    }

    @Operation(summary = "내 정보 수정", description = "로그인한 사용자의 닉네임과 프로필 이미지를 수정합니다. null 인 값은 변경하지 않습니다.")
    @PatchMapping("/me/info")
    public ApiResponse<MemberResponse.MemberSimpleResponse> updateInfo(
            @RequestBody @Valid UpdateMemberInfoRequest request,
            @AuthenticatedMember Member member
    ) {
        return ApiResponse.ok(memberCommandService.updateInfo(request, member));
    }

    @Operation(summary = "닉네임 중복 확인", description = "닉네임이 이미 사용 중인지 확인합니다.")
    @GetMapping("/nickname/exists")
    public ApiResponse<Boolean> checkNicknameExists(@RequestParam String nickname) {
        boolean exists = memberQueryService.existsByNickname(nickname);
        return ApiResponse.ok(exists);
    }
}
