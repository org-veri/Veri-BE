package org.veri.be.domain.reading.model

/**
 * Reading ID Value Object
 *
 * v2.1: Type-safe ID wrapper with validation
 */
@JvmInline
value class ReadingId(val value: Long) {
    init {
        require(value > 0) { "Invalid ReadingId: $value (must be positive)" }
    }

    companion object {
        fun of(value: Long): ReadingId {
            return ReadingId(value)
        }
    }
}
