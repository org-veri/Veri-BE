package org.veri.be.image;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.veri.be.global.auth.context.AuthenticatedMember;
import org.veri.be.global.response.PageResponse;
import org.veri.be.image.service.ImageCommandService;
import org.veri.be.image.service.ImageQueryService;
import org.veri.be.member.entity.Member;
import org.veri.be.lib.response.ApiResponse;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "이미지")
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class ImageController {

    private final ImageCommandService imageCmdService;
    private final ImageQueryService imageQueryService;

    @Operation(summary = "이미지 OCR 처리 및 저장", description = "이미지 URL을 받아 OCR을 수행하고 결과를 저장합니다.")
    @PostMapping("/v0/images/ocr")
    public ApiResponse<String> ocrImage(
            @AuthenticatedMember Member member,
            @RequestParam("imageUrl") String imageUrl) {
        return ApiResponse.ok(imageCmdService.processWithMistral(member, imageUrl)); // textract 제거 예정
    }

    @Operation(summary = "Mistral OCR API를 통한 이미지 텍스트 추출",
            description = "Mistral OCR API를 사용하여 이미지 URL에서 텍스트를 추출합니다.")
    @PostMapping("/v1/images/ocr")
    public ApiResponse<String> extractTextFromImageUrl(
            @AuthenticatedMember Member member,
            @RequestParam("imageUrl") String imageUrl) {
        return ApiResponse.ok(imageCmdService.processWithMistral(member, imageUrl));
    }

    @Operation(summary = "업로드 이미지 목록 조회", description = "내가 업로드한 이미지 파일 목록을 페이지네이션으로 조회합니다.")
    @GetMapping("/v0/images")
    public ApiResponse<PageResponse<List<String>>> getImageFiles(
            @RequestParam(defaultValue = "1") @Min(value = 1) int page,
            @RequestParam(defaultValue = "5") @Min(value = 1) int size,
            @AuthenticatedMember Member member
    ) {
        Pageable pageable = PageRequest.of(page - 1, size); // 백 페이지네이션 시에는 1-based index 를 0으로 보정.
        return ApiResponse.ok(imageQueryService.fetchUploadedImages(member.getId(), pageable));
    }
}
