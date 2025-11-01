package org.veri.be.global.storage.dto;

public record PresignedUrlResponse(
        String presignedUrl,
        String publicUrl
) {
}
