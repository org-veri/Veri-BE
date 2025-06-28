package org.goorm.veri.veribe.domain.card.service;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

public interface CardCommandService {

    Long createCard(Long memberId, String content, String imageUrl, Long memberBookId);

    void deleteCard(Long memberId, Long cardId);

    PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request);

    PresignedPostForm getPresignedPost();
}
