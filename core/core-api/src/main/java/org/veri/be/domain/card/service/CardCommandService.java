package org.veri.be.domain.card.service;

import org.veri.be.global.storage.dto.PresignedPostFormResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.veri.be.book.entity.Reading;
import org.veri.be.book.repository.ReadingRepository;
import org.veri.be.domain.card.controller.dto.CardConverter;
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse;
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.entity.CardErrorInfo;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.member.entity.Member;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.global.storage.service.StorageUtil;
import org.veri.be.lib.exception.ApplicationException;
import org.veri.be.lib.exception.CommonErrorCode;

import java.time.Duration;

import static org.veri.be.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class CardCommandService {

    private final CardRepository cardRepository;
    private final ReadingRepository readingRepository;
    private final StorageService storageService;

    @Transactional
    public Long createCard(Member member, String content, String imageUrl, Long memberBookId, Boolean isPublic) {
        Reading reading = readingRepository.findById(memberBookId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.INVALID_REQUEST));

        Card card = Card.builder()
                .member(member)
                .content(content)
                .image(imageUrl)
                .reading(reading)
                .isPublic(reading.isPublic() && isPublic) // 독서가 비공개면 카드도 무조건 비공개
                .build();

        cardRepository.save(card);
        return card.getId();
    }

    @Transactional
    public CardUpdateResponse updateCard(Member member, Long cardId, String content, String imageUrl) {
        Card card = this.getCard(cardId);
        Card updatedCard = card.updateContent(content, imageUrl, member);
        Card savedCard = cardRepository.save(updatedCard);

        return CardConverter.toCardUpdateResponse(savedCard);
    }

    public Card getCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND));
    }

    @Transactional
    public CardVisibilityUpdateResponse modifyVisibility(Member member, Long cardId, boolean isPublic) {
        Card card = this.getCard(cardId);
        card.authorizeOrThrow(member.getId());
        card.changeVisibility(member, isPublic);
        cardRepository.save(card);
        return new CardVisibilityUpdateResponse(card.getId(), card.isPublic());
    }

    @Transactional
    public void deleteCard(Member member, Long cardId) {
        Card card = getCard(cardId);
        card.authorizeOrThrow(member.getId());

        cardRepository.deleteById(cardId);
    }

    public PresignedUrlResponse getPresignedUrlForOcr(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        String prefix = "public/ocr";

        if (request.contentLength() > 3 * MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrl(
                request.contentType(),
                request.contentLength(),
                prefix,
                Duration.ofMinutes(expirationMinutes)
        );
    }

    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        String prefix = "public";

        if (request.contentLength() > 3 * MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrl(
                request.contentType(),
                request.contentLength(),
                prefix,
                Duration.ofMinutes(expirationMinutes)
        );
    }

    public PresignedPostFormResponse getPresignedPost() {
        String allowedContentType = "image/*";
        int expirationMinutes = 5;
        long allowedSize = 3 * MB; // 1MB
        String prefix = "public";

        return storageService.generatePresignedPost(
                allowedContentType,
                allowedSize,
                prefix,
                Duration.ofMinutes(expirationMinutes)
        );
    }
}
