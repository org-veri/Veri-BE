package org.veri.be.domain.image.client;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MistralOcrAdapter implements OcrPort {

    private final MistralOcrClient mistralOcrClient;

    @Override
    public String requestOcr(String imageUrl) {
        return mistralOcrClient.requestOcr(imageUrl);
    }
}
