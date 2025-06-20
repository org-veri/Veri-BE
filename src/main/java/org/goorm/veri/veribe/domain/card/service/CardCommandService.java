package org.goorm.veri.veribe.domain.card.service;

import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

public interface CardCommandService {

    PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request);
}
