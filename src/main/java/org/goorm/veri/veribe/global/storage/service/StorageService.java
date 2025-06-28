package org.goorm.veri.veribe.global.storage.service;


import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.time.Duration;

public interface StorageService {
    PresignedUrlResponse generatePresignedUrl(String contentType,
                                              Duration duration,
                                              long fileSize,
                                              String prefix
    );
}
