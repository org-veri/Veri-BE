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
import org.veri.be.domain.book.client.BookSearchClient
import org.veri.be.domain.book.entity.Book
import org.veri.be.domain.book.repository.BookRepository
import org.veri.be.domain.book.service.BookService
import org.veri.be.lib.exception.CommonErrorCode
import org.veri.be.support.assertion.ExceptionAssertions
import java.util.Optional

@ExtendWith(MockitoExtension::class)
class BookServiceTest {

    @org.mockito.Mock
    private lateinit var bookRepository: BookRepository

    @org.mockito.Mock
    private lateinit var bookSearchClient: BookSearchClient

    private lateinit var bookService: BookService

    @org.mockito.Captor
    private lateinit var bookCaptor: ArgumentCaptor<Book>

    @BeforeEach
    fun setUp() {
        bookService = BookService(bookRepository, bookSearchClient)
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

    @Nested
    @DisplayName("getBookById")
    inner class GetBookById {

        @Test
        @DisplayName("bookId가 null이면 null을 반환한다")
        fun returnsNullWhenIdNull() {
            val result = bookService.getBookById(null)

            assertThat(result).isNull()
        }

        @Test
        @DisplayName("존재하지 않으면 NotFoundException을 던진다")
        fun throwsWhenNotFound() {
            given(bookRepository.findById(1L)).willReturn(Optional.empty())

            ExceptionAssertions.assertApplicationException(
                { bookService.getBookById(1L) },
                CommonErrorCode.RESOURCE_NOT_FOUND
            )
        }
    }

    @Nested
    @DisplayName("searchBook")
    inner class SearchBook {

        @Test
        @DisplayName("검색 결과를 변환해 반환한다")
        fun returnsSearchResponse() {
            val item = org.veri.be.domain.book.dto.book.NaverBookItem()
            ReflectionTestUtils.setField(item, "title", "title")
            ReflectionTestUtils.setField(item, "author", "author")
            ReflectionTestUtils.setField(item, "image", "https://example.com/book.png")
            ReflectionTestUtils.setField(item, "publisher", "publisher")
            ReflectionTestUtils.setField(item, "isbn", "isbn-1")

            val naverResponse = org.veri.be.domain.book.dto.book.NaverBookResponse()
            ReflectionTestUtils.setField(naverResponse, "items", listOf(item))
            ReflectionTestUtils.setField(naverResponse, "total", 1)
            ReflectionTestUtils.setField(naverResponse, "start", 1)
            ReflectionTestUtils.setField(naverResponse, "display", 10)

            given(bookSearchClient.search("query", 1, 10)).willReturn(naverResponse)

            val result = bookService.searchBook("query", 1, 10)

            assertThat(result.books()).hasSize(1)
            assertThat(result.books()[0].title).isEqualTo("title")
            assertThat(result.books()[0].author).isEqualTo("author")
        }
    }
}
