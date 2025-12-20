package org.veri.be.slice.persistence.book;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.veri.be.domain.book.entity.Book;
import org.veri.be.domain.book.repository.BookRepository;
import org.veri.be.slice.persistence.PersistenceSliceTestSupport;

class BookRepositoryTest extends PersistenceSliceTestSupport {

    @Autowired
    BookRepository bookRepository;

    @Nested
    @DisplayName("findBookByIsbn")
    class FindBookByIsbn {

        @Test
        @DisplayName("isbn으로 도서를 조회한다")
        void returnsBookByIsbn() {
            Book book = bookRepository.save(Book.builder()
                    .image("https://example.com/book.png")
                    .title("title")
                    .author("author")
                    .isbn("isbn-1")
                    .build());

            Optional<Book> found = bookRepository.findBookByIsbn("isbn-1");

            assertThat(found).isPresent();
            assertThat(found.get().getId()).isEqualTo(book.getId());
        }

        @Test
        @DisplayName("존재하지 않는 isbn이면 빈 결과를 반환한다")
        void returnsEmptyWhenNotFound() {
            Optional<Book> found = bookRepository.findBookByIsbn("isbn-404");

            assertThat(found).isEmpty();
        }
    }
}
