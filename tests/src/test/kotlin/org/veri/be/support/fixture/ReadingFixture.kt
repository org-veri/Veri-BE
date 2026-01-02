package org.veri.be.support.fixture

import org.veri.be.domain.book.entity.Reading
import org.veri.be.domain.book.entity.enums.ReadingStatus

object ReadingFixture {
    fun aReading(): Reading.ReadingBuilder<*, *> {
        return Reading.builder()
            .member(MemberFixture.aMember().build())
            .book(BookFixture.aBook().build())
            .status(ReadingStatus.NOT_START)
            .isPublic(true)
    }
}
