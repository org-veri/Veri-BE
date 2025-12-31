package org.veri.be.domain.reading.model

/**
 * Reading Card Entity (v2.1)
 *
 * Pure state object - visibility controlled by Reading(AR)
 */
data class ReadingCard(
    val id: CardId?,  // v2.1: null before persistence
    val readingId: ReadingId,
    val memberId: Long,
    val content: CardContent,
    val imageUrl: String,
    val deleted: Boolean = false  // v2.1: renamed from isDeleted
) {
    /**
     * Update content (immutable - returns new instance)
     */
    fun updateContent(newContent: CardContent, newImageUrl: String): ReadingCard {
        return copy(
            content = newContent,
            imageUrl = newImageUrl
        )
    }

    /**
     * Check if readable by requester
     *
     * v2.1: Basic policy only - complex rules in Application layer
     */
    fun assertReadableBy(requesterId: Long) {
        // Reading visibility check is done by Reading(AR)
        // This is just owner check
        if (memberId != requesterId) {
            throw IllegalArgumentException("NOT_READABLE: Not your card")
        }
    }

    companion object {
        /**
         * Create new card
         */
        fun create(
            readingId: ReadingId,
            memberId: Long,
            content: CardContent,
            imageUrl: String
        ): ReadingCard {
            return ReadingCard(
                id = null,
                readingId = readingId,
                memberId = memberId,
                content = content,
                imageUrl = imageUrl,
                deleted = false
            )
        }

        /**
         * Restore from persistence
         */
        fun restore(
            id: Long,
            readingId: ReadingId,
            memberId: Long,
            content: String,
            imageUrl: String,
            deleted: Boolean
        ): ReadingCard {
            return ReadingCard(
                id = CardId.of(id),
                readingId = readingId,
                memberId = memberId,
                content = CardContent.of(content),
                imageUrl = imageUrl,
                deleted = deleted
            )
        }
    }
}
