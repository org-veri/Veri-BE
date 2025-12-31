package org.veri.be.domain.reading.model

/**
 * Card ID Value Object
 *
 * v2.1: Type-safe ID wrapper with validation
 */
@JvmInline
value class CardId(val value: Long) {
    init {
        require(value > 0) { "Invalid CardId: $value (must be positive)" }
    }

    companion object {
        fun of(value: Long): CardId {
            return CardId(value)
        }
    }
}
