package org.veri.be.integration.support.stub

import org.veri.be.image.service.OcrService

class StubOcrService : OcrService {
    override fun extract(imageUrl: String?): String {
        if (imageUrl != null && imageUrl.contains("error")) {
            throw RuntimeException("Simulated OCR failure")
        }
        return "Stub OCR Result"
    }
}
