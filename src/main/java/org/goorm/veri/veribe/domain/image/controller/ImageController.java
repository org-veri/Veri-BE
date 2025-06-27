package org.goorm.veri.veribe.domain.image.controller;

import lombok.RequiredArgsConstructor;

import org.goorm.veri.veribe.domain.image.dto.response.PageResponse;
import org.goorm.veri.veribe.domain.image.service.ImageCommandService;
import org.goorm.veri.veribe.domain.image.service.ImageQueryService;

import org.namul.api.payload.response.DefaultResponse;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;


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
            @RequestParam("file") MultipartFile file) throws Exception {
        return DefaultResponse.ok(imageCmdService.processImageOcrAndSave(file));
    }

    @GetMapping
    public DefaultResponse<PageResponse<String>> getImageFiles(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "5") int size
    ) throws IOException {
        // TODO: 인증 정보 도입
        Pageable pageable = PageRequest.of(page - 1, size); // 백 페이지네이션 시에는 1-based index 를 0으로 보정.
        return DefaultResponse.ok(imageQueryService.fetchUploadedImages(1L, pageable));
    }
}
