package org.veri.be.image.dto;

public record OcrResult(
        String imageUrl,
        String preProcessedUrl,
        String resultText,
        String ocrService
) {
}
