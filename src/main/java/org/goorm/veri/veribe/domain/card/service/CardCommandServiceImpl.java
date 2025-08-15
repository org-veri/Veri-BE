package org.goorm.veri.veribe.domain.card.service;

import io.github.miensoap.s3.core.post.dto.PresignedPostForm;
import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.book.repository.MemberBookRepository;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.exception.CardErrorInfo;
import org.goorm.veri.veribe.domain.card.repository.CardRepository;
import org.goorm.veri.veribe.domain.member.entity.Member;
import org.goorm.veri.veribe.global.exception.http.BadRequestException;
import org.goorm.veri.veribe.global.exception.http.ForbiddenException;
import org.goorm.veri.veribe.global.exception.http.NotFoundException;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class CardCommandServiceImpl implements CardCommandService {

    private final CardRepository cardRepository;
    private final MemberBookRepository memberBookRepository;
    private final StorageService storageService;

    @Transactional
    @Override
    public Long createCard(Member member, String content, String imageUrl, Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new BadRequestException(CardErrorInfo.BAD_REQUEST));

        Card card = Card.builder()
                .member(member)
                .content(content)
                .image(imageUrl)
                .memberBook(memberBook)
                .build();

        cardRepository.save(card);
        return card.getId();
    }

    @Transactional
    @Override
    public void deleteCard(Long memberId, Long cardId) {
        Card card = getCard(cardId);

        if (!card.getMember().getId().equals(memberId)) {
            throw new ForbiddenException(CardErrorInfo.FORBIDDEN);
        }

        cardRepository.deleteById(cardId);
    }

    @Override
    public PresignedUrlResponse getPresignedUrlForOcr(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        String prefix = "public/ocr";

        if (request.contentLength() > MB) {
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

    @Override
    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        String prefix = "public";

        if (request.contentLength() > MB) {
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

    @Override
    public PresignedPostForm getPresignedPost() {
        String allowedContentType = "image/*";
        int expirationMinutes = 5;
        long allowedSize = MB; // 1MB
        String prefix = "public";

        return storageService.generatePresignedPost(
                allowedContentType,
                allowedSize,
                prefix,
                Duration.ofMinutes(expirationMinutes)
        );
    }

    @Transactional
    @Override
    public Card updateCard(Long id, Long cardId, String content) {
        Card card = this.getCard(cardId);
        if (!card.getMember().getId().equals(id)) {
            throw new ForbiddenException(CardErrorInfo.FORBIDDEN);
        }

        Card updatedCard = card.toBuilder()
                .content(content)
                .build();

        return cardRepository.save(updatedCard);
    }

    public Card getCard(Long cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new NotFoundException(CardErrorInfo.NOT_FOUND));
    }
}
