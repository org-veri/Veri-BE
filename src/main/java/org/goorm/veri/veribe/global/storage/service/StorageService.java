package org.goorm.veri.veribe.global.storage.service;


import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.springframework.web.multipart.MultipartFile;

import java.awt.image.BufferedImage;
import java.time.Duration;

public interface StorageService {
    PresignedUrlResponse generatePresignedUrl(String contentType,
                                              long contentLength,
                                              String prefix,
                                              Duration duration
    );

    PresignedPostForm generatePresignedPost(String contentType,
                                            long fileSize,
                                            String prefix,
                                            Duration duration
    );
}
