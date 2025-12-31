package org.veri.be.domain.card.service

import org.veri.be.global.storage.dto.PresignedPostFormResponse
import org.springframework.context.ApplicationEventPublisher
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.veri.be.domain.book.entity.Reading as ReadingEntity
import org.veri.be.domain.book.repository.ReadingRepository
import org.veri.be.domain.card.controller.dto.CardConverter
import org.veri.be.domain.card.controller.dto.response.CardUpdateResponse
import org.veri.be.domain.card.controller.dto.response.CardVisibilityUpdateResponse
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.card.entity.CardErrorInfo
import org.veri.be.domain.card.repository.CardRepository
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.reading.event.CardCreatedEvent
import org.veri.be.domain.reading.event.CardDeletedEvent
import org.veri.be.domain.reading.event.CardUpdatedEvent
import org.veri.be.domain.reading.event.CardVisibilityChangedEvent
import org.veri.be.domain.reading.mapper.ReadingMapper
import org.veri.be.domain.reading.model.CardId
import org.veri.be.domain.reading.model.CardContent
import org.veri.be.domain.reading.model.Reading
import org.veri.be.domain.reading.model.ReadingCard
import org.veri.be.global.storage.dto.PresignedUrlRequest
import org.veri.be.global.storage.dto.PresignedUrlResponse
import org.veri.be.global.storage.service.StorageConstants
import org.veri.be.global.storage.service.StorageService
import org.veri.be.global.storage.service.StorageUtil
import org.veri.be.lib.exception.ApplicationException
import org.veri.be.lib.exception.CommonErrorCode
import java.time.Duration

/**
 * Card Command Service (v2.1)
 *
 * Uses Domain Model for business logic
 * Publishes domain events using Spring's ApplicationEventPublisher
 */
@Service
class CardCommandService(
    private val cardRepository: CardRepository,
    private val readingRepository: ReadingRepository,
    private val storageService: StorageService,
    private val eventPublisher: ApplicationEventPublisher
) {

    @Transactional
    fun createCard(
        member: Member,
        content: String,
        imageUrl: String,
        memberBookId: Long,
        isPublic: Boolean
    ): Long {
        val readingEntity: ReadingEntity = readingRepository.findById(memberBookId)
            .orElseThrow { ApplicationException.of(CommonErrorCode.INVALID_REQUEST) }

        // Use domain model for validation
        val reading: Reading = ReadingMapper.toDomain(readingEntity)

        // Domain model validation: Card visibility depends on Reading visibility
        val actualIsPublic = reading.isPublic && isPublic

        val card = Card.builder()
            .member(member)
            .content(content)
            .image(imageUrl)
            .reading(readingEntity)
            .isPublic(actualIsPublic) // Reading이 비공개면 카드도 무조건 비공개
            .build()

        cardRepository.save(card)

        // Publish event
        eventPublisher.publishEvent(
            CardCreatedEvent(
                cardId = card.id!!,
                readingId = memberBookId,
                memberId = member.id!!,
                content = content,
                imageUrl = imageUrl,
                isPublic = actualIsPublic
            )
        )

        return card.id
    }

    @Transactional
    fun updateCard(
        member: Member,
        cardId: Long,
        content: String,
        imageUrl: String
    ): CardUpdateResponse {
        val card: Card = this.getCard(cardId)

        // Use entity's existing business logic
        val updatedCard: Card = card.updateContent(content, imageUrl, member)
        val savedCard: Card = cardRepository.save(updatedCard)

        // Publish event
        eventPublisher.publishEvent(
            CardUpdatedEvent(
                cardId = cardId,
                readingId = card.reading?.id!!,
                memberId = member.id!!,
                content = content,
                imageUrl = imageUrl
            )
        )

        return CardConverter.toCardUpdateResponse(savedCard)
    }

    fun getCard(cardId: Long): Card {
        return cardRepository.findById(cardId)
            .orElseThrow { ApplicationException.of(CommonErrorCode.RESOURCE_NOT_FOUND) }
    }

    @Transactional
    fun modifyVisibility(
        member: Member,
        cardId: Long,
        isPublic: Boolean
    ): CardVisibilityUpdateResponse {
        val card: Card = this.getCard(cardId)
        card.authorizeOrThrow(member.id)

        // Use entity's existing business logic
        card.changeVisibility(member, isPublic)
        cardRepository.save(card)

        // Publish event
        eventPublisher.publishEvent(
            CardVisibilityChangedEvent(
                cardId = cardId,
                readingId = card.reading?.id!!,
                memberId = member.id!!,
                isPublic = card.isPublic
            )
        )

        return CardVisibilityUpdateResponse(card.id, card.isPublic)
    }

    @Transactional
    fun deleteCard(member: Member, cardId: Long) {
        val card: Card = getCard(cardId)
        card.authorizeOrThrow(member.id)

        cardRepository.deleteById(cardId)

        // Publish event
        eventPublisher.publishEvent(
            CardDeletedEvent(
                cardId = cardId,
                readingId = card.reading?.id!!,
                memberId = member.id!!
            )
        )
    }

    fun getPresignedUrlForOcr(request: PresignedUrlRequest): PresignedUrlResponse {
        val expirationMinutes = 5
        val prefix = "public/ocr"

        if (request.contentLength() > 3 * StorageConstants.MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE)
        }

        if (!StorageUtil.isImage(request.contentType())) {
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE)
        }

        return storageService.generatePresignedUrl(
            request.contentType(),
            request.contentLength(),
            prefix,
            Duration.ofMinutes(expirationMinutes.toLong())
        )
    }

    fun getPresignedUrl(request: PresignedUrlRequest): PresignedUrlResponse {
        val expirationMinutes = 5
        val prefix = "public"

        if (request.contentLength() > 3 * StorageConstants.MB) {
            throw ApplicationException.of(CardErrorInfo.IMAGE_TOO_LARGE)
        }

        if (!StorageUtil.isImage(request.contentType())) {
            throw ApplicationException.of(CardErrorInfo.UNSUPPORTED_IMAGE_TYPE)
        }

        return storageService.generatePresignedUrl(
            request.contentType(),
            request.contentLength(),
            prefix,
            Duration.ofMinutes(expirationMinutes.toLong())
        )
    }

    fun getPresignedPost(): PresignedPostFormResponse {
        val allowedContentType = "image/*"
        val expirationMinutes = 5
        val allowedSize = 3 * StorageConstants.MB
        val prefix = "public"

        return storageService.generatePresignedPost(
            allowedContentType,
            allowedSize,
            prefix,
            Duration.ofMinutes(expirationMinutes.toLong())
        )
    }
}
