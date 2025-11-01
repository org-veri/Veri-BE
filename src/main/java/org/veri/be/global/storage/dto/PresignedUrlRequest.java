package org.veri.be.global.storage.dto;

public record PresignedUrlRequest (
        String contentType,
        long contentLength
){}
