package org.veri.be.unit.book

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.BDDMockito.given
import org.mockito.Mockito.never
import org.mockito.Mockito.verify
import org.mockito.junit.jupiter.MockitoExtension
import org.springframework.test.util.ReflectionTestUtils
import org.veri.be.book.entity.Book
import org.veri.be.book.service.BookRepository
import org.veri.be.book.service.BookCommandService
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BookCommandServiceTest {

    @org.mockito.Mock
    private lateinit var bookRepository: BookRepository

    private lateinit var bookService: BookCommandService

    @org.mockito.Captor
    private lateinit var bookCaptor: ArgumentCaptor<Book>

    @BeforeEach
    fun setUp() {
        bookService = BookCommandService(bookRepository)
    }

    @Nested
    @DisplayName("addBook")
    inner class AddBook {

        @Test
        @DisplayName("이미 존재하면 기존 ID를 반환한다")
        fun returnsExistingId() {
            val existing = Book.builder()
                .id(1L)
                .title("title")
                .author("author")
                .isbn("isbn-1")
                .image("https://example.com/book.png")
                .build()
            given(bookRepository.findBookByIsbn("isbn-1")).willReturn(Optional.of(existing))

            val result = bookService.addBook("title", "https://example.com/book.png", "author", "publisher", "isbn-1")

            assertThat(result).isEqualTo(1L)
            verify(bookRepository, never()).save(any(Book::class.java))
        }

        @Test
        @DisplayName("존재하지 않으면 신규 도서를 저장한다")
        fun savesNewBook() {
            given(bookRepository.findBookByIsbn("isbn-1")).willReturn(Optional.empty())
            given(bookRepository.save(any(Book::class.java))).willAnswer { invocation ->
                val book = invocation.getArgument<Book>(0)
                ReflectionTestUtils.setField(book, "id", 2L)
                book
            }

            val result = bookService.addBook("title", "https://example.com/book.png", "author", "publisher", "isbn-1")

            verify(bookRepository).save(bookCaptor.capture())
            val saved = bookCaptor.value
            assertThat(saved.title).isEqualTo("title")
            assertThat(saved.author).isEqualTo("author")
            assertThat(saved.publisher).isEqualTo("publisher")
            assertThat(saved.isbn).isEqualTo("isbn-1")
            assertThat(result).isEqualTo(2L)
        }
    }

}
