package org.goorm.veri.veribe.domain.image.controller;

import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.image.service.ImageCommandService;
import org.goorm.veri.veribe.domain.image.service.ImageQueryService;

import org.namul.api.payload.response.DefaultResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;


@RestController
@RequestMapping("/api/v0/images")
@RequiredArgsConstructor
@CrossOrigin(value = "http://localhost:63342")
public class ImageController {
    public final ImageCommandService imageCmdService;
    public final ImageQueryService imageQueryService;

    @PostMapping
    public DefaultResponse<String> postImageFile(
            //@AuthenticatedMember Member member,
            @RequestParam("imageUrl") String imageUrl) throws Exception {
        return DefaultResponse.ok(imageCmdService.processImageOcrAndSave(imageUrl));
    }

    @GetMapping
    public DefaultResponse<PageResponse<List<String>>> getImageFiles(
            @RequestParam(defaultValue = "1") @Min(value = 1) int page,
            @RequestParam(defaultValue = "5") @Min(value = 1) int size
    ) throws IOException {
        // TODO: 인증 정보 도입
        Pageable pageable = PageRequest.of(page - 1, size); // 백 페이지네이션 시에는 1-based index 를 0으로 보정.
        return DefaultResponse.ok(imageQueryService.fetchUploadedImages(1L, pageable));
    }
}
