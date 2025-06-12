package org.goorm.veri.veribe.domain.card.service;

import lombok.RequiredArgsConstructor;
import org.goorm.veri.veribe.domain.book.entity.MemberBook;
import org.goorm.veri.veribe.domain.book.repository.MemberBookRepository;
import org.goorm.veri.veribe.domain.card.entity.Card;
import org.goorm.veri.veribe.domain.card.exception.CardErrorCode;
import org.goorm.veri.veribe.domain.card.exception.CardException;
import org.goorm.veri.veribe.domain.card.repository.CardRepository;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlRequest;
import org.goorm.veri.veribe.global.storage.dto.PresignedUrlResponse;
import org.goorm.veri.veribe.global.storage.service.StorageService;
import org.goorm.veri.veribe.global.storage.service.StorageUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

import static org.goorm.veri.veribe.domain.card.exception.CardErrorCode.BAD_REQUEST;
import static org.goorm.veri.veribe.global.storage.service.StorageConstants.MB;

@Service
@RequiredArgsConstructor
public class CardCommandServiceImpl implements CardCommandService {

    private final CardRepository cardRepository;
    private final MemberBookRepository memberBookRepository;
    private final StorageService storageService;

    @Transactional
    @Override
    public Long createCard(Long userId, String content, String imageUrl, Long memberBookId) {
        MemberBook memberBook = memberBookRepository.findById(memberBookId)
                .orElseThrow(() -> new CardException(BAD_REQUEST));

        Card card = Card.builder()
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
        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new CardException(NOT_FOUND));

        if (!card.getMemberBook().getMember().getId().equals(memberId)) {
            throw new CardException(FORBIDDEN);
        }

        cardRepository.deleteById(cardId);
    }

    @Override
    public PresignedUrlResponse getPresignedUrl(PresignedUrlRequest request) {
        int expirationMinutes = 5;
        long allowedSize = MB; // 1MB
        String prefix = "public";

        if (!StorageUtil.isImage(request.contentType())) throw new CardException(CardErrorCode.UNSUPPORTED_IMAGE_TYPE);

        return storageService.generatePresignedUrl(
                request.contentType(),
                Duration.ofMinutes(expirationMinutes),
                allowedSize,
                prefix
        );
    }
}
