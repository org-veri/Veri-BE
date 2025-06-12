package org.goorm.veri.veribe.domain.card.service;

import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

public interface CardCommandService {

    Long createCard(Long userId, String content, String imageUrl, Long memberBookId);

    PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request);
}
