package org.goorm.veri.veribe.global.storage.dto;

public record PresignedUrlResponse(
        String presignedUrl,
        String publicUrl,
        String imageKey
) {
}
