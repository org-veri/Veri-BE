package org.goorm.veri.veribe.global.storage.service;


import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

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

    PresignedPostForm generatePresignedPost(String contentType,
                                            long fileSize,
                                            String prefix,
                                            Duration duration
    );
}
