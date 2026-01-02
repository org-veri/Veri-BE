package org.veri.be.support.fixture

import org.veri.be.domain.book.entity.Book

object BookFixture {
    fun aBook(): Book.BookBuilder<*, *> {
        return Book.builder()
            .image("https://example.com/book.png")
            .title("title")
            .author("author")
            .publisher("publisher")
            .isbn("ISBN-1")
    }
}
