package org.goorm.veri.veribe.domain.image.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.auth.annotation.AuthenticatedMember;
import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.image.service.ImageCommandService;
import org.goorm.veri.veribe.domain.image.service.ImageQueryService;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.response.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import java.util.List;

@Tag(name = "이미지 API")
@RestController
@RequestMapping("/api/v0/images")
@RequiredArgsConstructor
public class ImageController {
    public final ImageCommandService imageCmdService;
    public final ImageQueryService imageQueryService;

    @Operation(summary = "이미지 OCR 처리 및 저장", description = "이미지 URL을 받아 OCR을 수행하고 결과를 저장합니다.")
    @PostMapping("/ocr")
    public ApiResponse<String> ocrImage(
            @AuthenticatedMember Member member,
            @RequestParam("imageUrl") String imageUrl) throws Exception {
        return ApiResponse.ok(imageCmdService.processImageOcrAndSave(member, imageUrl));
    }

    @Operation(summary = "업로드 이미지 목록 조회", description = "내가 업로드한 이미지 파일 목록을 페이지네이션으로 조회합니다.")
    @GetMapping
    public ApiResponse<PageResponse<List<String>>> getImageFiles(
            @RequestParam(defaultValue = "1") @Min(value = 1) int page,
            @RequestParam(defaultValue = "5") @Min(value = 1) int size,
            @AuthenticatedMember Member member
    ) {
        Pageable pageable = PageRequest.of(page - 1, size); // 백 페이지네이션 시에는 1-based index 를 0으로 보정.
        return ApiResponse.ok(imageQueryService.fetchUploadedImages(member.getId(), pageable));
    }
}
