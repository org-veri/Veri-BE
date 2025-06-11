package org.goorm.veri.veribe.global.storage.service;


import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

import java.time.Duration;

public interface StorageService {
    PresignedUrlResponse generatePresignedUrl(String contentType, Duration duration);
}
