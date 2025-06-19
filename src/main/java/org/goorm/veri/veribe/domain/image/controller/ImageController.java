package org.goorm.veri.veribe.domain.image.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.service.ImageCommandService;
import org.goorm.veri.veribe.domain.image.service.ImageQueryService;
import org.namul.api.payload.response.DefaultResponse;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

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
    public DefaultResponse<String> postImageFile(@RequestParam("file") MultipartFile file) throws Exception {
        return DefaultResponse.ok(imageCmdService.extractTextFromBook(file));
    }

    @GetMapping
    public DefaultResponse<List<String>> getImageFiles() throws IOException {
        return DefaultResponse.ok(imageQueryService.fetchUploadedImages("test1"));
    }
}
