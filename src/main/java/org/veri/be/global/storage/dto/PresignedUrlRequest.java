package org.veri.be.global.storage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public record PresignedUrlRequest (
        @NotBlank
        String contentType,
        @Positive
        long contentLength
){}
