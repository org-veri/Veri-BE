package org.veri.be.support.fixture

import org.veri.be.domain.card.entity.Card

object CardFixture {
    fun aCard(): Card.CardBuilder<*, *> {
        return Card.builder()
            .content("content")
            .image("https://example.com/card.png")
            .member(MemberFixture.aMember().build())
            .reading(ReadingFixture.aReading().build())
            .isPublic(false)
    }
}
