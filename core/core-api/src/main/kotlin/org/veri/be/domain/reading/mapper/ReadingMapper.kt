package org.veri.be.domain.reading.mapper

import org.veri.be.domain.book.entity.Reading as ReadingEntity
import org.veri.be.domain.card.entity.Card
import org.veri.be.domain.member.entity.Member
import org.veri.be.domain.reading.model.Reading
import org.veri.be.domain.reading.model.ReadingCard
import org.veri.be.domain.reading.model.ReadingId
import org.veri.be.domain.reading.model.CardId

/**
 * Reading Entity-Domain Mapper (v2.1)
 *
 * Strategy B: Separate + Mapper pattern
 */
object ReadingMapper {

    /**
     * Convert Domain -> Entity
     * Note: memberId/bookId are not in Entity, only relationships exist
     * Relationships (member, book) are set by repository during persistence
     */
    fun toEntity(domain: Reading): ReadingEntity {
        return ReadingEntity.builder()
            .id(domain.id?.value)
            .status(domain.status)
            .isPublic(domain.isPublic)
            .startedAt(domain.startedAt)
            .endedAt(domain.endedAt)
            .score(domain.score)
            .member(null)  // Set by repository
            .book(null)    // Set by repository
            .build()
    }

    /**
     * Convert Entity -> Domain
     * Extract memberId/bookId from relationships
     */
    fun toDomain(entity: ReadingEntity): Reading {
        val cards = entity.cards?.map { toDomainCard(it) } ?: emptyList()
        return Reading.restore(
            id = entity.id!!,
            memberId = entity.member?.id ?: throw IllegalArgumentException("Reading must have a member"),
            bookId = entity.book?.id ?: throw IllegalArgumentException("Reading must have a book"),
            status = entity.status,
            isPublic = entity.isPublic,
            startedAt = entity.startedAt,
            endedAt = entity.endedAt,
            score = entity.score,
            cards = cards
        )
    }

    /**
     * Convert Card Entity -> ReadingCard Domain
     */
    private fun toDomainCard(card: Card): ReadingCard {
        return ReadingCard.restore(
            id = card.id!!,
            readingId = ReadingId.of(card.reading?.id ?: throw IllegalArgumentException("Card must have a reading")),
            memberId = card.member?.id ?: throw IllegalArgumentException("Card must have a member"),
            content = card.content,
            imageUrl = card.image,
            deleted = !card.isPublic  // ReadingCard uses 'deleted' instead of 'isPublic'
        )
    }

    /**
     * Convert ReadingCard Domain -> Card Entity (partial)
     * Note: This creates a new Card for persistence
     * Reading and Member relationships must be set by caller
     */
    fun toCardEntity(card: ReadingCard): Card {
        return Card.builder()
            .id(card.id?.value)
            .content(card.content.value)  // CardContent -> String
            .image(card.imageUrl)
            .isPublic(!card.deleted)  // ReadingCard 'deleted' maps to Card 'isPublic' (inverted)
            .reading(null)  // Set by caller
            .member(null)   // Set by caller
            .build()
    }
}
