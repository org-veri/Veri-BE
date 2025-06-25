package org.goorm.veri.veribe.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.image.exception.ImageErrorCode;
import org.goorm.veri.veribe.domain.image.exception.ImageException;
import org.goorm.veri.veribe.domain.image.repository.ImageRepository;
import org.springframework.stereotype.Service;

import java.io.File;

import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ImageQueryServiceImpl implements ImageQueryService{
    private final ImageRepository imageRepository;

    @Override
    public List<String> fetchUploadedImages(Long userId) {
        List<String> publicUrls = imageRepository.findByMemberId(userId);

        if (publicUrls.isEmpty()) {
            return List.of();
        }

        return publicUrls.stream()
                .map(this::downloadAndEncodeToBase64)
                .toList();
    }

    private String downloadAndEncodeToBase64(String imageUrl) {
        try {
            byte[] imageBytes = new URL(imageUrl).openStream().readAllBytes();
            return Base64.getEncoder().encodeToString(imageBytes);
        } catch (IOException e) {
            throw new ImageException(ImageErrorCode.ENCODING_FAILED); // 새로 정의 필요
        }
    }
}
