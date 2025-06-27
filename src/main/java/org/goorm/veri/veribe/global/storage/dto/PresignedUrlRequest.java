package org.goorm.veri.veribe.global.storage.dto;

public record PresignedUrlRequest (
        String contentType,
        long contentLength
){}
