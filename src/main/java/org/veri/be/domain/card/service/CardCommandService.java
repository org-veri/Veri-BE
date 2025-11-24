package org.veri.be.domain.card.service;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import lombok.RequiredArgsConstructor;
import org.veri.be.domain.book.entity.Reading;
import org.veri.be.domain.book.repository.ReadingRepository;
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse;
import org.veri.be.domain.card.entity.Card;
import org.veri.be.domain.card.exception.CardErrorInfo;
import org.veri.be.domain.card.repository.CardRepository;
import org.veri.be.domain.member.entity.Member;
import org.veri.be.global.auth.context.MemberContext;
import org.veri.be.lib.exception.http.BadRequestException;
import org.veri.be.lib.exception.http.NotFoundException;
import org.veri.be.global.storage.dto.PresignedUrlRequest;
import org.veri.be.global.storage.dto.PresignedUrlResponse;
import org.veri.be.global.storage.service.StorageService;
import org.veri.be.global.storage.service.StorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
                .orElseThrow(() -> new BadRequestException(CardErrorInfo.BAD_REQUEST));

        Card card = Card.builder()
                .member(member)
                .content(content)
                .image(imageUrl)
                .reading(reading)
                .isPublic(reading.getIsPublic() && isPublic) // 독서가 비공개면 카드도 무조건 비공개
                .build();

        cardRepository.save(card);
        return card.getId();
    }

    @Transactional
    public Card updateCard(Long cardId, String content) {
        Card card = this.getCard(cardId);
        card.authorizeMember(MemberContext.getMemberOrThrow().getId());

        Card updatedCard = card.toBuilder()
                .content(content)
                .build();

        return cardRepository.save(updatedCard);
    }

    public Card getCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(CardErrorInfo.NOT_FOUND));
    }

    @Transactional
    public CardVisibilityUpdateResponse modifyVisibility(Long cardId, boolean isPublic) {
        Card card = this.getCard(cardId);
        card.authorizeMember(MemberContext.getMemberOrThrow().getId());

        if (isPublic) {
            card.setPublic();
        } else {
            card.setPrivate();
        }

        cardRepository.save(card);
        return new CardVisibilityUpdateResponse(card.getId(), card.getIsPublic());
    }

    @Transactional
    public void deleteCard(Long cardId) {
        Card card = getCard(cardId);
        card.authorizeMember(MemberContext.getMemberOrThrow().getId());

        cardRepository.deleteById(cardId);
    }

    public PresignedUrlResponse getPresignedUrlForOcr(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        String prefix = "public/ocr";

        if (request.contentLength() > 3 * MB) {
            throw new BadRequestException(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw new BadRequestException(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

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
            throw new BadRequestException(CardErrorInfo.IMAGE_TOO_LARGE);
        }

        if (!StorageUtil.isImage(request.contentType()))
            throw new BadRequestException(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrl(
                request.contentType(),
                request.contentLength(),
                prefix,
                Duration.ofMinutes(expirationMinutes)
        );
    }

    public PresignedPostForm getPresignedPost() {
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
