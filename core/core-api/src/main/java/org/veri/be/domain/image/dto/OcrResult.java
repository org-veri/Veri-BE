package org.veri.be.domain.image.dto;

public record OcrResult(
        String imageUrl,
        String preProcessedUrl,
        String resultText,
        String ocrService
) {
}
