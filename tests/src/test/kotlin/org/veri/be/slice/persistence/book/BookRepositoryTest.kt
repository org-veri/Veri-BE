package org.veri.be.slice.persistence.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.slice.persistence.PersistenceSliceTestSupport

class BookRepositoryTest : PersistenceSliceTestSupport() {

    @Autowired
    private lateinit var bookRepository: BookRepository

    @Nested
    @DisplayName("findBookByIsbn")
    inner class FindBookByIsbn {

        @Test
        @DisplayName("isbn으로 도서를 조회한다")
        fun returnsBookByIsbn() {
            val book = bookRepository.save(
                Book.builder()
                    .image("https://example.com/book.png")
                    .title("title")
                    .author("author")
                    .isbn("isbn-1")
                    .build()
            )

            val found = bookRepository.findBookByIsbn("isbn-1")

            assertThat(found).isPresent
            assertThat(found.get().id).isEqualTo(book.id)
        }

        @Test
        @DisplayName("존재하지 않는 isbn이면 빈 결과를 반환한다")
        fun returnsEmptyWhenNotFound() {
            val found = bookRepository.findBookByIsbn("isbn-404")

            assertThat(found).isEmpty
        }
    }
}
