package org.goorm.veri.veribe.domain.image.controller;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.service.ImageCommandService;
import org.namul.api.payload.response.DefaultResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/v0/images")
@RequiredArgsConstructor
@CrossOrigin(value = "http://localhost:63342")
public class ImageController {
    public final ImageCommandService imageCmdService;

    @PostMapping
    public DefaultResponse<String> postImageFile(@RequestParam("file") MultipartFile file) throws Exception {
        return DefaultResponse.ok(imageCmdService.extractTextFromBook(file));
    }
}
