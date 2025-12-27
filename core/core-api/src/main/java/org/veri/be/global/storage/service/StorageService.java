package org.veri.be.global.storage.service;


import org.veri.be.global.storage.dto.PresignedPostFormResponse;
import org.veri.be.global.storage.dto.PresignedUrlResponse;

import java.time.Duration;

public interface StorageService {
    PresignedUrlResponse generatePresignedUrl(String contentType,
                                              long contentLength,
                                              String prefix,
                                              Duration duration
    );

    /**
     * 기본 경로: public
     * 만료 시간: 5분
     */
    PresignedUrlResponse generatePresignedUrlOfDefault(String contentType,
                                                       long contentLength
    );

    PresignedPostFormResponse generatePresignedPost(String contentType,
                                                    long fileSize,
                                                    String prefix,
                                                    Duration duration
    );
}
