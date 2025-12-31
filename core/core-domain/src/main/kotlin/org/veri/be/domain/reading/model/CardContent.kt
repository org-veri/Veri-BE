package org.veri.be.domain.reading.model

/**
 * Card Content Value Object
 *
 * v2.1: Self-validating content with business rules
 */
@JvmInline
value class CardContent(val value: String) {
    init {
        require(value.isNotBlank()) { "Content cannot be blank" }
        require(value.length <= MAX_LENGTH) { "Content too long: ${value.length} (max $MAX_LENGTH)" }
    }

    companion object {
        const val MAX_LENGTH = 2000

        fun of(value: String): CardContent {
            return CardContent(value.trim())
        }
    }
}
