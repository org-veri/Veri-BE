package org.goorm.veri.veribe.domain.card.service;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import org.goorm.veri.veribe.domain.card.controller.dto.CardUpdateResponse;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;

public interface CardCommandService {

    Long createCard(Member member, String content, String imageUrl, Long memberBookId);

    void deleteCard(Long memberId, Long cardId);

    PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request);

    PresignedUrlResponse getPresignedUrlForOcr(PresignedUrlRequest request);

    PresignedPostForm getPresignedPost();

    Card updateCard(Long id, Long cardId, String content, String imageUrl);
}
