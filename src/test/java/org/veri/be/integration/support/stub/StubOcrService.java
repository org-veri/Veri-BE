package org.veri.be.integration.support.stub;

import org.veri.be.domain.image.service.OcrService;

public class StubOcrService implements OcrService {
    @Override
    public String extract(String imageUrl) {
        return "Stub OCR Result";
    }
}
