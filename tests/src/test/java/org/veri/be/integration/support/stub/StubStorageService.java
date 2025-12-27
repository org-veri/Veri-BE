package org.veri.be.integration.support.stub;

import org.veri.be.global.storage.dto.PresignedPostFormResponse;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;

import java.time.Duration;

public class StubStorageService implements StorageService {
    @Override
    public PresignedUrlResponse generatePresignedUrl(String contentType, long contentLength, String prefix, Duration duration) {
        return new PresignedUrlResponse("http://stub.presigned.url", null);
    }

    @Override
    public PresignedUrlResponse generatePresignedUrlOfDefault(String contentType, long contentLength) {
        return new PresignedUrlResponse("http://stub.presigned.url", null);
    }

    @Override
    public PresignedPostFormResponse generatePresignedPost(String contentType, long fileSize, String prefix, Duration duration) {
        return new PresignedPostFormResponse("http://stub.s3.url", java.util.Map.of("key", "value"));
    }
}
