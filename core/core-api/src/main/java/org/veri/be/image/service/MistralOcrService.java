package org.veri.be.image.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.veri.be.image.client.OcrPort;
import org.veri.be.image.exception.ImageErrorCode;
import org.veri.be.image.repository.OcrResultRepository;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.time.SleepSupport;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.Executor;

@Slf4j
@Service
public class MistralOcrService extends AbstractOcrService {

    private final OcrPort ocrClient;
    private final SleepSupport sleepSupport;
    private final Executor ocrExecutor;

    public MistralOcrService(
            OcrResultRepository ocrResultRepository,
            OcrPort ocrClient,
            SleepSupport sleepSupport,
            @Qualifier("ocrExecutor") Executor ocrExecutor
    ) {
        super(ocrResultRepository);
        this.ocrClient = ocrClient;
        this.sleepSupport = sleepSupport;
        this.ocrExecutor = ocrExecutor;
    }

    @Override
    protected String serviceName() {
        return "Mistral";
    }

    @Override
    protected String doExtract(String imageUrl) {
        try {
            sleepSupport.sleep(Duration.ofMillis(500));
        } catch (InterruptedException _) {
            Thread.currentThread().interrupt();
            throw ApplicationException.of(ImageErrorCode.OCR_PROCESSING_FAILED);
        }

        String extracted = tryExtractAsync(imageUrl);
        if (extracted != null) {
            saveOcrResult(imageUrl, null, extracted);
            return extracted;
        }

        String preprocessedUrl = getPreprocessedUrl(imageUrl);
        extracted = tryExtractAsync(preprocessedUrl);
        if (extracted != null) {
            saveOcrResult(imageUrl, preprocessedUrl, extracted);
            return extracted;
        }

        log.error("Mistral OCR 전처리/원본 모두 실패");
        throw ApplicationException.of(ImageErrorCode.OCR_PROCESSING_FAILED);
    }

    private String tryExtractAsync(String targetImageUrl) {
        try {
            return CompletableFuture
                    .supplyAsync(() -> ocrClient.requestOcr(targetImageUrl), ocrExecutor)
                    .join();
        } catch (CompletionException e) {
            log.warn("Mistral OCR 실패 ({}): {}", targetImageUrl, e.getCause() != null ? e.getCause().getMessage() : e.getMessage());
            return null;
        }
    }
}
