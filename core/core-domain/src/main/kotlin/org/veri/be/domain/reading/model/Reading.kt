package org.veri.be.domain.reading.model

import org.veri.be.domain.book.entity.enums.ReadingStatus
import java.time.Clock
import java.time.LocalDateTime

/**
 * Reading Aggregate Root (v2.1)
 *
 * Pure domain model - User×Book mapping center
 * - Manages ReadingCard entities (1:N)
 * - Controls card visibility (public/private transitions)
 * - Immutable with copy pattern
 */
data class Reading(
    val id: ReadingId?,  // v2.1: null before persistence
    val memberId: Long,
    val bookId: Long,
    val status: ReadingStatus,
    val isPublic: Boolean,
    val startedAt: LocalDateTime?,
    val endedAt: LocalDateTime?,
    val score: Double?,
    val cards: List<ReadingCard>
) {
    /**
     * Start reading (state transition)
     */
    fun start(clock: Clock): Reading {
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        return copy(
            status = ReadingStatus.READING,
            startedAt = now
        )
    }

    /**
     * Finish reading (state transition)
     */
    fun finish(clock: Clock): Reading {
        val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
        return copy(
            status = ReadingStatus.DONE,
            endedAt = now
        )
    }

    /**
     * Make reading and all cards public
     *
     * v2.1: Reading(AR) controls card visibility
     */
    fun makePublic(): Reading {
        return copy(
            isPublic = true,
            cards = this.cards.map { it.copy(deleted = false) }  // v2.1: Reading controls
        )
    }

    /**
     * Make reading and all cards private
     *
     * v2.1: Reading(AR) controls card visibility
     */
    fun makePrivate(): Reading {
        return copy(
            isPublic = false,
            cards = this.cards.map { it.copy(deleted = true) }  // v2.1: Reading controls
        )
    }

    /**
     * Add card to reading
     */
    fun addCard(card: ReadingCard): Reading {
        validateCardLimit()
        val newCards = this.cards.toMutableList()
        newCards.add(card)
        return copy(cards = newCards)
    }

    /**
     * Remove card from reading
     */
    fun removeCard(cardId: CardId): Reading {
        val newCards = this.cards.filter { it.id != cardId }
        return copy(cards = newCards)
    }

    /**
     * Reorder cards
     */
    fun reorderCards(cardIds: List<CardId>): Reading {
        val cardMap = this.cards.associateBy { it.id }
        val reordered = cardIds.mapNotNull { cardMap[it] }
        return copy(cards = reordered)
    }

    private fun validateCardLimit() {
        require(this.cards.size < MAX_CARD_COUNT) {
            "Maximum $MAX_CARD_COUNT cards allowed"
        }
    }

    companion object {
        const val MAX_CARD_COUNT = 100

        /**
         * Create new reading
         *
         * v2.1: Clock injected, id = null
         */
        fun create(memberId: Long, bookId: Long, clock: Clock): Reading {
            val now = LocalDateTime.now(clock).withSecond(0).withNano(0)
            return Reading(
                id = null,
                memberId = memberId,
                bookId = bookId,
                status = ReadingStatus.NOT_START,
                isPublic = true,
                startedAt = null,
                endedAt = null,
                score = null,
                cards = emptyList()
            )
        }

        /**
         * Restore from persistence
         *
         * v2.1: All fields non-null after persistence
         */
        fun restore(
            id: Long,
            memberId: Long,
            bookId: Long,
            status: ReadingStatus,
            isPublic: Boolean,
            startedAt: LocalDateTime?,
            endedAt: LocalDateTime?,
            score: Double?,
            cards: List<ReadingCard>
        ): Reading {
            return Reading(
                id = ReadingId.of(id),
                memberId = memberId,
                bookId = bookId,
                status = status,
                isPublic = isPublic,
                startedAt = startedAt,
                endedAt = endedAt,
                score = score,
                cards = cards
            )
        }
    }
}
