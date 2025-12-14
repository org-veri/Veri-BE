package org.veri.be.domain.image.service;

import lombok.RequiredArgsConstructor;
import org.veri.be.domain.image.entity.OcrResult;
import org.veri.be.domain.image.repository.OcrResultRepository;

@RequiredArgsConstructor
public abstract class AbstractOcrService implements OcrService {

    protected final OcrResultRepository ocrResultRepository;

    @Override
    public final String extract(String imageUrl) {
        return doExtract(imageUrl);
    }

    protected String getPreprocessedUrl(String imageUrl) {
        String preprocessedImageUrl = imageUrl.replaceFirst("/ocr/", "/ocr-preprocessed/");
        int lastDotIndex = preprocessedImageUrl.lastIndexOf('.');
        if (lastDotIndex != -1) {
            preprocessedImageUrl = preprocessedImageUrl.substring(0, lastDotIndex) + ".jpg";
        } else {
            preprocessedImageUrl += ".jpg";
        }
        return preprocessedImageUrl;
    }

    protected void saveOcrResult(String originalImageUrl, String preprocessedUrlOrNull, String text) {
        OcrResult result = OcrResult.builder()
                .resultText(text)
                .imageUrl(originalImageUrl)
                .preProcessedUrl(preprocessedUrlOrNull)
                .ocrService(serviceName())
                .build();
        ocrResultRepository.save(result);
    }

    protected abstract String serviceName();

    protected abstract String doExtract(String imageUrl);
}
